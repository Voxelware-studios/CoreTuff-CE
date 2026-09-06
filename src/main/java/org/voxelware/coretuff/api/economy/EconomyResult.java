package org.voxelware.coretuff.api.economy;

import java.util.Objects;

public final class EconomyResult {

    private final boolean success;
    private final double balance;
    private final String message;

    private EconomyResult(boolean success, double balance, String message) {
        this.success = success;
        this.balance = balance;
        this.message = message;
    }

    public static EconomyResult success(double balance) {
        return new EconomyResult(true, balance, null);
    }

    public static EconomyResult failure(String message) {
        return new EconomyResult(false, 0.0, Objects.requireNonNull(message));
    }

    public boolean success() { return success; }
    public double balance() { return balance; }
    public String message() { return message; }
}
