package org.voxelware.coretuff.Features.Economy.Commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.voxelware.coretuff.Features.Economy.EconomyService;
import org.voxelware.coretuff.commandExecuter.PlayerCommand;

import java.util.List;

public class BalanceCommand extends PlayerCommand {

    private final EconomyService economyService;

    public BalanceCommand(EconomyService economyService) {
        this.economyService = economyService;
    }

    @Override
    public boolean onCommand(@NotNull Player player, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        var plugin = economyService.plugin();
        try {
            if (args.length == 0) {
                double balance = economyService.getBalance(player.getUniqueId());
                player.sendMessage(plugin.format(player, economyService.config().currencySymbol() + " balance: " + economyService.formatter().format(balance), null));
                return true;
            }

            if (!player.hasPermission("coretuff.economy.balance.others")) {
                player.sendMessage(plugin.format(player, "&cYou don't have permission to check others' balances.", null));
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (target == null || (!target.isOnline() && !target.hasPlayedBefore())) {
                player.sendMessage(plugin.format(player, "&cPlayer not found.", null));
                return true;
            }

            double balance = economyService.getBalance(target.getUniqueId());
            player.sendMessage(plugin.format(player, target.getName() + "'s " + economyService.config().currencySymbol() + " balance: " + economyService.formatter().format(balance), null));
        } catch (Exception e) {
            player.sendMessage(plugin.format(player, "&cAn error occurred while fetching balance.", null));
        }
        return true;
    }

    @Override
    protected List<String> generateCompletions(org.bukkit.command.CommandSender sender, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission("coretuff.economy.balance.others")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return List.of();
    }
}
