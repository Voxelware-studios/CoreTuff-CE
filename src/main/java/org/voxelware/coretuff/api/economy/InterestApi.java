package org.voxelware.coretuff.api.economy;

/**
 * Read-only view of the interest system.
 * All methods are safe to call from any thread.
 */
public interface InterestApi {

    boolean isEnabled();

    double getInterestRate();

    long getIntervalHours();

    double getMaxBalance();

    String getBypassPermission();
}
