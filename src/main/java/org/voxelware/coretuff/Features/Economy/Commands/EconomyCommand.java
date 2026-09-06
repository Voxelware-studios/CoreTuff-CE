package org.voxelware.coretuff.Features.Economy.Commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Features.Economy.EconomyService;
import org.voxelware.coretuff.commandExecuter.BaseCommand;

import java.util.ArrayList;
import java.util.List;

public class EconomyCommand extends BaseCommand {

    private final EconomyService economyService;

    public EconomyCommand(EconomyService economyService) {
        this.economyService = economyService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        var plugin = economyService.plugin();
        Player player = sender instanceof Player p ? p : null;

        if (!sender.hasPermission("coretuff.economy.admin")) {
            sender.sendMessage(plugin.format(player, "&cYou don't have permission to use this command.", null));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(plugin.format(player, "&cUsage: /eco <give|take|set|reset> <player> <amount>", null));
            return true;
        }

        String sub = args[0].toLowerCase();
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null || (!target.isOnline() && !target.hasPlayedBefore())) {
            sender.sendMessage(plugin.format(player, "&cPlayer not found.", null));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.format(player, "&cInvalid amount.", null));
            return true;
        }

        if (amount < 0) {
            sender.sendMessage(plugin.format(player, "&cAmount must be positive.", null));
            return true;
        }

        try {
            switch (sub) {
                case "give" -> {
                    economyService.adminGive(target.getUniqueId(), amount);
                    sender.sendMessage(plugin.format(player, "&aGave " + economyService.formatter().format(amount) + " to " + target.getName(), null));
                    if (target.isOnline()) {
                        Player onlineTarget = target.getPlayer();
                        if (onlineTarget != null) {
                            onlineTarget.sendMessage(plugin.format(onlineTarget, "&aReceived " + economyService.formatter().format(amount), null));
                        }
                    }
                }
                case "take" -> {
                    boolean success = economyService.withdraw(target.getUniqueId(), amount);
                    if (!success) {
                        double bal = economyService.getBalance(target.getUniqueId());
                        sender.sendMessage(plugin.format(player, "&c" + target.getName() + " only has " + economyService.formatter().format(bal), null));
                        return true;
                    }
                    sender.sendMessage(plugin.format(player, "&aTook " + economyService.formatter().format(amount) + " from " + target.getName(), null));
                }
                case "set" -> {
                    economyService.adminSet(target.getUniqueId(), amount);
                    sender.sendMessage(plugin.format(player, "&aSet " + target.getName() + "'s balance to " + economyService.formatter().format(amount), null));
                }
                case "reset" -> {
                    economyService.adminSet(target.getUniqueId(), economyService.config().startingBalance());
                    sender.sendMessage(plugin.format(player, "&aReset " + target.getName() + "'s balance to " + economyService.formatter().format(economyService.config().startingBalance()), null));
                }
                default -> {
                    sender.sendMessage(plugin.format(player, "&cUsage: /eco <give|take|set|reset> <player> <amount>", null));
                }
            }
        } catch (Exception e) {
            sender.sendMessage(plugin.format(player, "&cAn error occurred.", null));
        }
        return true;
    }

    @Override
    protected List<String> generateCompletions(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("coretuff.economy.admin")) {
            return List.of();
        }

        if (args.length == 1) {
            return List.of("give", "take", "set", "reset");
        }
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        if (args.length == 3 && !args[0].equalsIgnoreCase("reset")) {
            return List.of("<amount>");
        }
        return List.of();
    }
}
