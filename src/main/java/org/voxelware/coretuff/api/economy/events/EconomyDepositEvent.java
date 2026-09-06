package org.voxelware.coretuff.api.economy.events;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import java.util.UUID;

public class EconomyDepositEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID playerUuid;
    private final double amount;
    private final String reason;

    public EconomyDepositEvent(UUID playerUuid, double amount, String reason) {
        super(true);
        this.playerUuid = playerUuid;
        this.amount = amount;
        this.reason = reason;
    }

    public UUID playerUuid() { return playerUuid; }
    public double amount() { return amount; }
    public String reason() { return reason; }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
