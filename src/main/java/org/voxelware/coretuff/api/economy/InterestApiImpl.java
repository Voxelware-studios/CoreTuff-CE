package org.voxelware.coretuff.api.economy;

import org.voxelware.coretuff.Features.Economy.EconomyConfig;

public final class InterestApiImpl implements InterestApi {

    private final EconomyConfig config;

    public InterestApiImpl(EconomyConfig config) {
        this.config = config;
    }

    @Override
    public boolean isEnabled() {
        return config.interestEnabled();
    }

    @Override
    public double getInterestRate() {
        return config.interestRatePercent();
    }

    @Override
    public long getIntervalHours() {
        return config.interestIntervalHours();
    }

    @Override
    public double getMaxBalance() {
        return config.interestMaxBalance();
    }

    @Override
    public String getBypassPermission() {
        return config.interestBypassPermission();
    }
}
