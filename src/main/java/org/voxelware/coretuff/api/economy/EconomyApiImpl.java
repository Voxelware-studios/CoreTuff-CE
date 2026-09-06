package org.voxelware.coretuff.api.economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Features.Economy.EconomyService;
import org.voxelware.coretuff.Features.Economy.Transaction.TransactionRepository;
import org.voxelware.coretuff.Features.Economy.Transaction.TransactionType;
import org.voxelware.coretuff.api.economy.events.BalanceChangeEvent;
import org.voxelware.coretuff.api.economy.events.EconomyDepositEvent;
import org.voxelware.coretuff.api.economy.events.EconomyTransferEvent;
import org.voxelware.coretuff.api.economy.events.EconomyWithdrawEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public final class EconomyApiImpl implements EconomyApi {

    private final CoreTuff plugin;
    private final EconomyService service;
    private final TransactionRepository txRepo;

    public EconomyApiImpl(CoreTuff plugin, EconomyService service, TransactionRepository txRepo) {
        this.plugin = plugin;
        this.service = service;
        this.txRepo = txRepo;
    }

    @Override
    public double getBalance(UUID player) {
        try {
            return service.getBalance(player);
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public boolean hasBalance(UUID player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public EconomyResult deposit(UUID player, double amount, String reason) {
        if (amount <= 0) return EconomyResult.failure("Amount must be positive");
        try {
            double before = service.getBalance(player);
            service.deposit(player, amount);
            double after = service.getBalance(player);
            txRepo.log(null, player, amount, TransactionType.ADMIN_GIVE, reason != null ? reason : "API deposit");
            Bukkit.getPluginManager().callEvent(new EconomyDepositEvent(player, amount, reason));
            Bukkit.getPluginManager().callEvent(new BalanceChangeEvent(player, before, after, reason));
            return EconomyResult.success(after);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Deposit failed for " + player, e);
            return EconomyResult.failure("Deposit failed: " + e.getMessage());
        }
    }

    @Override
    public EconomyResult withdraw(UUID player, double amount, String reason) {
        if (amount <= 0) return EconomyResult.failure("Amount must be positive");
        try {
            double before = service.getBalance(player);
            if (before < amount) return EconomyResult.failure("Insufficient funds");
            EconomyWithdrawEvent event = new EconomyWithdrawEvent(player, amount, reason);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return EconomyResult.failure("Withdraw cancelled by event");
            service.withdraw(player, amount);
            double after = service.getBalance(player);
            txRepo.log(player, null, amount, TransactionType.ADMIN_REMOVE, reason != null ? reason : "API withdraw");
            Bukkit.getPluginManager().callEvent(new BalanceChangeEvent(player, before, after, reason));
            return EconomyResult.success(after);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Withdraw failed for " + player, e);
            return EconomyResult.failure("Withdraw failed: " + e.getMessage());
        }
    }

    @Override
    public EconomyResult transfer(UUID from, UUID to, double amount, String reason) {
        if (amount <= 0) return EconomyResult.failure("Amount must be positive");
        if (from.equals(to)) return EconomyResult.failure("Cannot transfer to yourself");
        try {
            double senderBal = service.getBalance(from);
            if (senderBal < amount) return EconomyResult.failure("Insufficient funds");
            EconomyTransferEvent event = new EconomyTransferEvent(from, to, amount, reason);
            Bukkit.getPluginManager().callEvent(event);
            double beforeFrom = service.getBalance(from);
            double beforeTo = service.getBalance(to);
            service.withdraw(from, amount);
            service.deposit(to, amount);
            double afterTo = service.getBalance(to);
            txRepo.log(from, to, amount, TransactionType.PAY, reason != null ? reason : "API transfer");
            Bukkit.getPluginManager().callEvent(new BalanceChangeEvent(from, beforeFrom, service.getBalance(from), reason));
            Bukkit.getPluginManager().callEvent(new BalanceChangeEvent(to, beforeTo, afterTo, reason));
            return EconomyResult.success(afterTo);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Transfer failed from " + from + " to " + to, e);
            return EconomyResult.failure("Transfer failed: " + e.getMessage());
        }
    }

    @Override
    public String format(double amount) {
        return service.formatter().format(amount);
    }

    @Override
    public String getCurrencyName() {
        return service.config().currencyName();
    }

    @Override
    public String getCurrencySymbol() {
        return service.config().currencySymbol();
    }

    @Override
    public List<BalanceEntry> getTopBalances(int amount) {
        return service.getTopBalances(amount).stream()
                .map(entry -> {
                    String name = "Unknown";
                    try {
                        OfflinePlayer p = Bukkit.getOfflinePlayer(entry.getKey());
                        name = p.getName() != null ? p.getName() : "Unknown";
                    } catch (Exception ignored) {}
                    return new BalanceEntry(entry.getKey(), name, entry.getValue());
                })
                .toList();
    }
}
