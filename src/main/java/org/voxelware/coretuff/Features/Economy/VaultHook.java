package org.voxelware.coretuff.Features.Economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.ServicePriority;
import org.voxelware.coretuff.CoreTuff;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public final class VaultHook implements Economy {

    private final CoreTuff plugin;
    private final EconomyService economyService;
    private final EconomyConfig config;
    private final CurrencyFormatter formatter;

    public VaultHook(CoreTuff plugin, EconomyService economyService, EconomyConfig config, CurrencyFormatter formatter) {
        this.plugin = plugin;
        this.economyService = economyService;
        this.config = config;
        this.formatter = formatter;
    }

    public void register() {
        Bukkit.getServicesManager().register(Economy.class, this, plugin, ServicePriority.Highest);
        plugin.getLogger().log(Level.INFO, "Vault economy hook registered.");
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public String getName() {
        return "CoreTuff";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return config.decimalPlaces();
    }

    @Override
    public String format(double amount) {
        return formatter.format(amount);
    }

    @Override
    public String currencyNamePlural() {
        return config.currencyNamePlural();
    }

    @Override
    public String currencyNameSingular() {
        return config.currencyName();
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return hasAccount(player.getUniqueId());
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return getBalance(player.getUniqueId());
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        return withdrawPlayer(player.getUniqueId(), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        return depositPlayer(player.getUniqueId(), amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return createPlayerAccount(player.getUniqueId());
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }

    @Override
    public boolean hasAccount(String playerName) {
        return Bukkit.getOfflinePlayer(playerName) != null;
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public double getBalance(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player == null) return 0.0;
        return getBalance(player.getUniqueId());
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player == null) {
            return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "Player not found.");
        }
        return withdrawPlayer(player.getUniqueId(), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player == null) {
            return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "Player not found.");
        }
        return depositPlayer(player.getUniqueId(), amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player == null) return false;
        return createPlayerAccount(player.getUniqueId());
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }

    private boolean hasAccount(UUID uuid) {
        try {
            return economyService.getBalance(uuid) >= 0;
        } catch (Exception e) {
            return false;
        }
    }

    private double getBalance(UUID uuid) {
        try {
            return economyService.getBalance(uuid);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private EconomyResponse withdrawPlayer(UUID uuid, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative amounts.");
        }
        try {
            double balance = economyService.getBalance(uuid);
            if (balance < amount) {
                return new EconomyResponse(amount, balance, EconomyResponse.ResponseType.FAILURE, "Insufficient funds.");
            }
            economyService.withdraw(uuid, amount);
            return new EconomyResponse(amount, economyService.getBalance(uuid), EconomyResponse.ResponseType.SUCCESS, null);
        } catch (Exception e) {
            return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "Error processing withdrawal.");
        }
    }

    private EconomyResponse depositPlayer(UUID uuid, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative amounts.");
        }
        try {
            economyService.deposit(uuid, amount);
            return new EconomyResponse(amount, economyService.getBalance(uuid), EconomyResponse.ResponseType.SUCCESS, null);
        } catch (Exception e) {
            return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "Error processing deposit.");
        }
    }

    private boolean createPlayerAccount(UUID uuid) {
        try {
            economyService.getBalance(uuid);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banking not supported.");
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banking not supported.");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banking not supported.");
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banking not supported.");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banking not supported.");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banking not supported.");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banking not supported.");
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banking not supported.");
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banking not supported.");
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banking not supported.");
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banking not supported.");
    }

    @Override
    public List<String> getBanks() {
        return Collections.emptyList();
    }
}
