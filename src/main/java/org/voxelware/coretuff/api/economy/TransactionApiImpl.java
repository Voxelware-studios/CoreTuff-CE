package org.voxelware.coretuff.api.economy;

import org.voxelware.coretuff.Features.Economy.Transaction.TransactionRepository;
import org.voxelware.coretuff.Features.Economy.Transaction.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TransactionApiImpl implements TransactionApi {

    private final TransactionRepository repo;
    private final Connection connection;

    public TransactionApiImpl(TransactionRepository repo, Connection connection) {
        this.repo = repo;
        this.connection = connection;
    }

    @Override
    public void logDeposit(UUID target, double amount, String reason) {
        try {
            repo.log(null, target, amount, TransactionType.ADMIN_GIVE, reason);
        } catch (Exception ignored) {}
    }

    @Override
    public void logWithdraw(UUID target, double amount, String reason) {
        try {
            repo.log(target, null, amount, TransactionType.ADMIN_REMOVE, reason);
        } catch (Exception ignored) {}
    }

    @Override
    public void logTransfer(UUID from, UUID to, double amount, String reason) {
        try {
            repo.log(from, to, amount, TransactionType.PAY, reason);
        } catch (Exception ignored) {}
    }

    @Override
    public List<TransactionEntry> getRecentTransactions(UUID player) {
        return getTransactions(player, 10);
    }

    @Override
    public List<TransactionEntry> getTransactions(UUID player, int limit) {
        List<TransactionEntry> result = new ArrayList<>();
        try (PreparedStatement st = connection.prepareStatement(
                "SELECT id, sender_uuid, target_uuid, amount, transaction_type, reason, created_at " +
                "FROM economy_transactions " +
                "WHERE sender_uuid = ? OR target_uuid = ? " +
                "ORDER BY created_at DESC LIMIT ?")) {
            st.setString(1, player.toString());
            st.setString(2, player.toString());
            st.setInt(3, Math.max(1, Math.min(limit, 100)));
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    result.add(new TransactionEntry(
                            rs.getLong("id"),
                            rs.getString("sender_uuid") != null ? UUID.fromString(rs.getString("sender_uuid")) : null,
                            rs.getString("target_uuid") != null ? UUID.fromString(rs.getString("target_uuid")) : null,
                            rs.getDouble("amount"),
                            rs.getString("transaction_type"),
                            rs.getString("reason"),
                            rs.getLong("created_at")
                    ));
                }
            }
        } catch (Exception ignored) {}
        return result;
    }
}
