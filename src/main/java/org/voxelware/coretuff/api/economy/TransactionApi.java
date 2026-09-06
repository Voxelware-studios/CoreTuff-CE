package org.voxelware.coretuff.api.economy;

import java.util.List;
import java.util.UUID;

/**
 * Transaction logging API.
 * <p>
 * All methods perform I/O and should be called asynchronously.
 */
public interface TransactionApi {

    void logDeposit(UUID target, double amount, String reason);

    void logWithdraw(UUID target, double amount, String reason);

    void logTransfer(UUID from, UUID to, double amount, String reason);

    List<TransactionEntry> getRecentTransactions(UUID player);

    List<TransactionEntry> getTransactions(UUID player, int limit);
}
