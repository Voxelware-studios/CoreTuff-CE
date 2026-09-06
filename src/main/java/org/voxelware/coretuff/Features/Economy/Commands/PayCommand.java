package org.voxelware.coretuff.Features.Economy.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.voxelware.coretuff.Features.Economy.EconomyService;
import org.voxelware.coretuff.commandExecuter.PlayerCommand;

import java.util.List;

public class PayCommand extends PlayerCommand {

    private final EconomyService economyService;

    public PayCommand(EconomyService economyService) {
        this.economyService = economyService;
    }

    @Override
    public boolean onCommand(@NotNull Player sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        var plugin = economyService.plugin();
        if (args.length < 2) {
            sender.sendMessage(plugin.format(sender, "&cUsage: /pay <player> <amount>", null));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(plugin.format(sender, "&cPlayer not found or offline.", null));
            return true;
        }

        if (sender.getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage(plugin.format(sender, "&cYou cannot pay yourself.", null));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.format(sender, "&cInvalid amount.", null));
            return true;
        }

        double minimum = economyService.config().minimumTransfer();
        if (amount < minimum) {
            sender.sendMessage(plugin.format(sender, "&cMinimum transfer amount: " + economyService.formatter().format(minimum), null));
            return true;
        }

        try {
            double senderBalance = economyService.getBalance(sender.getUniqueId());
            if (senderBalance < amount) {
                sender.sendMessage(plugin.format(sender, "&cInsufficient funds. You have " + economyService.formatter().format(senderBalance), null));
                return true;
            }

            if (!economyService.transfer(sender, target, amount)) {
                sender.sendMessage(plugin.format(sender, "&cTransaction failed.", null));
                return true;
            }

            sender.sendMessage(plugin.format(sender, "&aSent " + economyService.formatter().format(amount) + " to " + target.getName(), null));

            target.sendMessage(plugin.format(target, "&aReceived " + economyService.formatter().format(amount) + " from " + sender.getName(), null));
        } catch (Exception e) {
            sender.sendMessage(plugin.format(sender, "&cAn error occurred processing the transaction.", null));
        }
        return true;
    }

    @Override
    protected List<String> generateCompletions(org.bukkit.command.CommandSender sender, String label, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> !name.equals(sender.getName())).toList();
        }
        if (args.length == 2) {
            return List.of("<amount>");
        }
        return List.of();
    }
}
