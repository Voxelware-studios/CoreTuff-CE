package org.voxelware.coretuff.Features.Lands.Warps.Commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.voxelware.coretuff.Features.Lands.Warps.WarpService;
import org.voxelware.coretuff.commandExecuter.PlayerCommand;

import java.util.List;

public class SetWarpCommand extends PlayerCommand {

    private final WarpService warpService;

    public SetWarpCommand(WarpService warpService) {
        this.warpService = warpService;
    }

    @Override
    public boolean onCommand(@NotNull Player player, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!player.hasPermission("coretuff.land.warps.setwarp")) {
            player.sendMessage(warpService.plugin().format(player, "&cYou don't have permission to set warps.", null));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(warpService.plugin().format(player, "&cUsage: /setwarp <name> [description]", null));
            return true;
        }

        String name = args[0];
        if (!isValidName(name)) {
            player.sendMessage(warpService.plugin().format(player, "&cWarp name must be 1-32 alphanumeric characters.", null));
            return true;
        }

        try {
            if (warpService.warpExists(name)) {
                player.sendMessage(warpService.plugin().format(player, "&cWarp &f" + name + " &calready exists.", null));
                return true;
            }

            if (!warpService.canSetMoreWarps(player)) {
                int max = warpService.getMaxWarps(player);
                player.sendMessage(warpService.plugin().format(player, "&cYou can only set up to &f" + max + " &cwarps.", null));
                return true;
            }

            String description = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : null;
            warpService.setWarp(player, name, description);
            player.sendMessage(warpService.plugin().format(player, "&aWarp &f" + name + " &ahas been created.", null));
        } catch (Exception e) {
            player.sendMessage(warpService.plugin().format(player, "&cFailed to create warp.", null));
        }
        return true;
    }

    private boolean isValidName(String name) {
        return name.length() >= 1 && name.length() <= 32 && name.matches("[a-zA-Z0-9_-]+");
    }

    @Override
    protected List<String> generateCompletions(org.bukkit.command.CommandSender sender, String label, String[] args) {
        if (args.length >= 2) {
            return List.of("<description>");
        }
        return List.of();
    }
}
