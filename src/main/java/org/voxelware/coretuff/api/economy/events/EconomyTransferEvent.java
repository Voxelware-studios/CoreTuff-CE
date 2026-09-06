package org.voxelware.coretuff.api.economy.events;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import java.util.UUID;

public class EconomyTransferEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID from;
    private final UUID to;
    private final double amount;
    private final String reason;

    public EconomyTransferEvent(UUID from, UUID to, double amount, String reason) {
        super(true);
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.reason = reason;
    }

    public UUID from() { return from; }
    public UUID to() { return to; }
    public double amount() { return amount; }
    public String reason() { return reason; }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
