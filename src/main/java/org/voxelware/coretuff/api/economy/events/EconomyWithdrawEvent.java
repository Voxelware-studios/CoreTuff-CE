package org.voxelware.coretuff.api.economy.events;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import java.util.UUID;

public class EconomyWithdrawEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID playerUuid;
    private final double amount;
    private final String reason;
    private boolean cancelled;

    public EconomyWithdrawEvent(UUID playerUuid, double amount, String reason) {
        super(true);
        this.playerUuid = playerUuid;
        this.amount = amount;
        this.reason = reason;
    }

    public UUID playerUuid() { return playerUuid; }
    public double amount() { return amount; }
    public String reason() { return reason; }
    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
