package org.voxelware.coretuff.Features.Lands.Warps.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Features.Lands.Warps.Warp;
import org.voxelware.coretuff.Features.Lands.Warps.WarpService;
import org.voxelware.coretuff.Utility.CoreTuffProvider;
import org.voxelware.coretuff.commandExecuter.PlayerCommand;

import java.util.List;
import java.util.Map;

public class WarpCommand extends PlayerCommand {

    private final WarpService warpService;

    public WarpCommand(WarpService warpService) {
        this.warpService = warpService;
    }

    @Override
    public boolean onCommand(@NotNull Player player, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1 || args.length > 2) {
            player.sendMessage(warpService.plugin().format(player, "&cUsage: /warp <name> [player]", null));
            return true;
        }

        String warpName = args[0];

        Player target = player;
        if (args.length == 2) {
            if (!player.hasPermission("coretuff.land.warps.warp.others")) {
                player.sendMessage(warpService.plugin().format(player, "&cYou don't have permission to warp others.", null));
                return true;
            }
            target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(warpService.plugin().format(player, "&cPlayer not found.", null));
                return true;
            }
        }

        try {
            Warp warp = warpService.getWarp(warpName);
            if (warp == null) {
                player.sendMessage(warpService.plugin().format(player, "&cWarp &f" + warpName + " &cdoes not exist.", null));
                return true;
            }

            var location = warpService.repository().toLocation(warp);
            if (location == null) {
                player.sendMessage(warpService.plugin().format(player, "&cWarp world is no longer available.", null));
                return true;
            }

            double cost = warpService.config().warpCost();
            boolean freeWarp = cost <= 0 || player.hasPermission(warpService.config().warpCostBypassPermission());

            if (!freeWarp && !warpService.hasSufficientBalance(player)) {
                String costStr = warpService.warpCostFormatted();
                player.sendMessage(warpService.plugin().format(player, "&cYou need " + (costStr != null ? costStr : "funds") + " to use this warp.", null));
                return true;
            }

            int delay = warpService.config().warpTeleportDelay();
            Player finalTarget = target;
            Player finalPlayer = player;
            CoreTuff plugin = warpService.plugin();
            CoreTuffProvider.getDelayedTeleporter().teleport(finalTarget, location, delay)
                    .whenComplete((success, throwable) -> {
                        if (Boolean.TRUE.equals(success)) {
                            if (!freeWarp) {
                                warpService.chargeWarpCost(player);
                            }
                            finalTarget.sendMessage(plugin.format(finalTarget, "&aTeleported to warp &f" + warp.getName() + "&a.", null));
                            if (!finalPlayer.equals(finalTarget)) {
                                finalPlayer.sendMessage(plugin.format(finalPlayer, "&aTeleported &f" + finalTarget.getName() + " &ato warp &f" + warp.getName() + "&a.", null));
                            }
                        }
                    });
        } catch (Exception e) {
            player.sendMessage(warpService.plugin().format(player, "&cAn error occurred.", null));
        }
        return true;
    }

    @Override
    protected List<String> generateCompletions(org.bukkit.command.CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) return List.of();
        try {
            if (args.length == 1) {
                return warpService.getAllWarps().stream().map(Warp::getName).toList();
            }
            if (args.length == 2 && sender.hasPermission("coretuff.land.warps.warp.others")) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            }
        } catch (Exception ignored) {}
        return List.of();
    }
}
