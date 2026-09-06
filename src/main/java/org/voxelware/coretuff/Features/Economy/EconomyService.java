package org.voxelware.coretuff.Features.Economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Features.Economy.Transaction.TransactionRepository;
import org.voxelware.coretuff.Features.Economy.Transaction.TransactionType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EconomyService {

    private final CoreTuff plugin;
    private final EconomyRepository repository;
    private final TransactionRepository transactionRepository;
    private final EconomyConfig config;
    private final CurrencyFormatter formatter;

    public EconomyService(
            CoreTuff plugin,
            EconomyRepository repository,
            TransactionRepository transactionRepository,
            EconomyConfig config,
            CurrencyFormatter formatter
    ) {
        this.plugin = plugin;
        this.repository = repository;
        this.transactionRepository = transactionRepository;
        this.config = config;
        this.formatter = formatter;
    }

    public CoreTuff plugin() {
        return plugin;
    }

    public EconomyConfig config() {
        return config;
    }

    public CurrencyFormatter formatter() {
        return formatter;
    }

    public double getBalance(UUID uuid) throws Exception {
        return repository.getBalance(uuid);
    }

    public void deposit(UUID uuid, double amount) throws Exception {
        double balance = repository.getBalance(uuid);
        double capped = Math.min(balance + amount, config.maxBalance());
        repository.setBalance(uuid, capped);
    }

    public boolean withdraw(UUID uuid, double amount) throws Exception {
        double balance = repository.getBalance(uuid);
        if (balance < amount) {
            return false;
        }
        repository.setBalance(uuid, balance - amount);
        return true;
    }

    public void setBalance(UUID uuid, double amount) throws Exception {
        double capped = Math.min(amount, config.maxBalance());
        repository.setBalance(uuid, Math.max(capped, 0));
    }

    public boolean transfer(Player sender, Player target, double amount) throws Exception {
        double senderBalance = repository.getBalance(sender.getUniqueId());

        if (senderBalance < amount) {
            return false;
        }

        boolean bypassTax = sender.hasPermission(config.taxBypassPermission());
        double taxPercent = bypassTax ? 0.0 : config.payTaxPercent();
        double tax = amount * (taxPercent / 100.0);
        double finalAmount = amount - tax;

        repository.setBalance(sender.getUniqueId(), senderBalance - amount);

        double targetBalance = repository.getBalance(target.getUniqueId());
        double capped = Math.min(targetBalance + finalAmount, config.maxBalance());
        repository.setBalance(target.getUniqueId(), capped);

        transactionRepository.log(sender.getUniqueId(), target.getUniqueId(), amount, TransactionType.PAY, "Player payment");
        if (tax > 0) {
            transactionRepository.log(null, target.getUniqueId(), tax, TransactionType.TAX, "Transfer tax");
        }

        return true;
    }

    public void adminGive(UUID target, double amount) throws Exception {
        deposit(target, amount);
        transactionRepository.log(null, target, amount, TransactionType.ADMIN_GIVE, "Admin give");
    }

    public void adminSet(UUID target, double amount) throws Exception {
        setBalance(target, amount);
        transactionRepository.log(null, target, amount, TransactionType.ADMIN_SET, "Admin set");
    }

    public void adminRemove(UUID target, double amount) throws Exception {
        withdraw(target, amount);
        transactionRepository.log(null, target, amount, TransactionType.ADMIN_REMOVE, "Admin remove");
    }

    public void applyInterest() {
        if (!config.interestEnabled()) {
            return;
        }

        double rate = config.interestRatePercent() / 100.0;
        double maxBal = config.interestMaxBalance();
        long interval = config.interestIntervalHours() * 3600000L;
        long now = System.currentTimeMillis();

        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                if (player.hasPermission(config.interestBypassPermission())) {
                    continue;
                }

                UUID uuid = player.getUniqueId();
                double balance = repository.getBalance(uuid);
                if (balance <= 0) continue;

                long lastInterest = repository.getLastInterestAt(uuid);
                if (lastInterest > 0 && (now - lastInterest) < interval) continue;

                double applicable = Math.min(balance, maxBal);
                double interest = applicable * rate;

                if (interest <= 0) continue;

                deposit(uuid, interest);
                repository.setLastInterestAt(uuid, now);
                transactionRepository.log(null, uuid, interest, TransactionType.INTEREST, "Interest earned");

                if (player.isOnline()) {
                    player.sendMessage(plugin.format(player,
                            "&aYou earned &f" + formatter.format(interest) + " &ain interest!",
                            null
                    ));
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to apply interest for " + player.getName());
            }
        }
    }

    public List<Map.Entry<UUID, Double>> getTopBalances(int limit) {
        try {
            List<Map.Entry<UUID, Double>> all = repository.getAllBalancesSorted();
            return all.stream()
                    .filter(entry -> {
                        OfflinePlayer p = Bukkit.getOfflinePlayer(entry.getKey());
                        if (p.isOp()) return false;
                        Player online = p.getPlayer();
                        if (online != null && online.hasPermission("coretuff.economy.lbignore")) return false;
                        return true;
                    })
                    .limit(Math.max(0, limit))
                    .toList();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to fetch top balances: " + e.getMessage());
            return List.of();
        }
    }
}
