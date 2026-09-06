package org.voxelware.coretuff.Features.Utility.Kits.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Features.Utility.Kits.Kit;
import org.voxelware.coretuff.Features.Utility.Kits.KitService;
import org.voxelware.coretuff.commandExecuter.PlayerCommand;

import java.util.List;

public class KitCommand extends PlayerCommand {

    private final KitService kitService;
    private final CoreTuff plugin;

    public KitCommand(KitService kitService, CoreTuff plugin) {
        this.kitService = kitService;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull Player player, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            player.sendMessage(plugin.format(player, "&cUsage: /kit <name> [player]", null));
            return true;
        }

        Kit kit = kitService.getKit(args[0]);
        if (kit == null) {
            player.sendMessage(plugin.format(player, "&cKit &f" + args[0] + " &cnot found.", null));
            return true;
        }

        Player target = player;
        if (args.length >= 2) {
            if (!player.hasPermission("coretuff.utility.kit.others")) {
                player.sendMessage(plugin.format(player, "&cYou don't have permission to give kits to others.", null));
                return true;
            }
            target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(plugin.format(player, "&cPlayer not found.", null));
                return true;
            }
        }

        if (kit.getPermission() != null && !kit.getPermission().isBlank() && !target.hasPermission(kit.getPermission())) {
            target.sendMessage(plugin.format(target, "&cYou don't have permission to use this kit.", null));
            return true;
        }

        long remaining = kitService.remainingCooldown(target, kit);
        if (remaining > 0) {
            target.sendMessage(plugin.format(target, "&cYou must wait " + formatDuration(remaining) + " before using this kit again.", null));
            return true;
        }

        kitService.redeemKit(target, kit);
        target.sendMessage(plugin.format(target, "&aYou received kit &f" + kit.getName() + "&a.", null));
        if (!target.equals(player)) {
            player.sendMessage(plugin.format(player, "&aGave kit &f" + kit.getName() + " &ato &f" + target.getName() + "&a.", null));
        }
        return true;
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        if (seconds < 60) return seconds + "s";
        long minutes = seconds / 60;
        seconds %= 60;
        if (minutes < 60) return minutes + "m " + seconds + "s";
        long hours = minutes / 60;
        minutes %= 60;
        if (hours < 24) return hours + "h " + minutes + "m";
        long days = hours / 24;
        hours %= 24;
        return days + "d " + hours + "h";
    }

    @Override
    protected List<String> generateCompletions(org.bukkit.command.CommandSender sender, String label, String[] args) {
        if (args.length == 1) {
            return kitService.getKitNames().stream().toList();
        }
        if (args.length == 2 && sender.hasPermission("coretuff.utility.kit.others")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return List.of();
    }
}
