package org.voxelware.coretuff.api.economy;

import java.util.Objects;
import java.util.UUID;

public final class BalanceEntry {

    private final UUID playerUuid;
    private final String playerName;
    private final double balance;

    public BalanceEntry(UUID playerUuid, String playerName, double balance) {
        this.playerUuid = Objects.requireNonNull(playerUuid);
        this.playerName = playerName;
        this.balance = balance;
    }

    public UUID playerUuid() { return playerUuid; }
    public String playerName() { return playerName; }
    public double balance() { return balance; }
}
