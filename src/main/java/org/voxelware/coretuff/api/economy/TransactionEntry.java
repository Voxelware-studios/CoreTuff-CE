package org.voxelware.coretuff.api.economy;

import java.util.Objects;
import java.util.UUID;

public final class TransactionEntry {

    private final long id;
    private final UUID senderUuid;
    private final UUID targetUuid;
    private final double amount;
    private final String type;
    private final String reason;
    private final long createdAt;

    public TransactionEntry(long id, UUID senderUuid, UUID targetUuid, double amount, String type, String reason, long createdAt) {
        this.id = id;
        this.senderUuid = senderUuid;
        this.targetUuid = targetUuid;
        this.amount = amount;
        this.type = Objects.requireNonNull(type);
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public long id() { return id; }
    public UUID senderUuid() { return senderUuid; }
    public UUID targetUuid() { return targetUuid; }
    public double amount() { return amount; }
    public String type() { return type; }
    public String reason() { return reason; }
    public long createdAt() { return createdAt; }
}
