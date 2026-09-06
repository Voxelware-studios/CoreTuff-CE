package org.voxelware.coretuff.api.economy.events;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import java.util.UUID;

public class BalanceChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID playerUuid;
    private final double oldBalance;
    private final double newBalance;
    private final String reason;

    public BalanceChangeEvent(UUID playerUuid, double oldBalance, double newBalance, String reason) {
        super(true);
        this.playerUuid = playerUuid;
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
        this.reason = reason;
    }

    public UUID playerUuid() { return playerUuid; }
    public double oldBalance() { return oldBalance; }
    public double newBalance() { return newBalance; }
    public String reason() { return reason; }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
