package org.voxelware.coretuff.Features.Economy;

import org.bukkit.configuration.file.FileConfiguration;

public final class EconomyConfig {

    private FileConfiguration config;

    public EconomyConfig() {
    }

    public void load(FileConfiguration config) {
        this.config = config;
    }

    public void reload() {
    }

    public FileConfiguration get() {
        return config;
    }

    public String currencyName() {
        if (config == null) return "Dollar";
        return config.getString("economy.currency.name", "Dollar");
    }

    public String currencyNamePlural() {
        if (config == null) return "Dollars";
        return config.getString("economy.currency.name-plural", "Dollars");
    }

    public String currencySymbol() {
        if (config == null) return "$";
        return config.getString("economy.currency.symbol", "$");
    }

    public int decimalPlaces() {
        if (config == null) return 2;
        return config.getInt("economy.currency.decimal-places", 2);
    }

    public double startingBalance() {
        if (config == null) return 0.0;
        return config.getDouble("economy.defaults.starting-balance", 0.0);
    }

    public double maxBalance() {
        if (config == null) return 999999999.99;
        return config.getDouble("economy.defaults.max-balance", 999999999.99);
    }

    public double payTaxPercent() {
        if (config == null) return 0.0;
        return config.getDouble("economy.transactions.pay-tax-percent", 0.0);
    }

    public double minimumTransfer() {
        if (config == null) return 0.01;
        return config.getDouble("economy.transactions.minimum-transfer", 0.01);
    }

    public String taxBypassPermission() {
        if (config == null) return "coretuff.economy.bypass-tax";
        return config.getString("economy.transactions.pay-tax-bypass-permission", "coretuff.economy.bypass-tax");
    }

    public boolean interestEnabled() {
        if (config == null) return false;
        return config.getBoolean("economy.interest.enabled", false);
    }

    public double interestRatePercent() {
        if (config == null) return 0.5;
        return config.getDouble("economy.interest.rate-percent", 0.5);
    }

    public int interestIntervalHours() {
        if (config == null) return 24;
        return config.getInt("economy.interest.interval-hours", 24);
    }

    public double interestMaxBalance() {
        if (config == null) return 100000.0;
        return config.getDouble("economy.interest.max-balance", 100000.0);
    }

    public String interestBypassPermission() {
        if (config == null) return "coretuff.economy.bypass-interest";
        return config.getString("economy.interest.interest-bypass-permission", "coretuff.economy.bypass-interest");
    }
}
