package org.voxelware.coretuff.Features.Economy.Transaction;

import org.voxelware.coretuff.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;

import java.util.UUID;

public class TransactionRepository {

    private final DatabaseManager databaseManager;

    public TransactionRepository(
            DatabaseManager databaseManager
    ) {

        this.databaseManager = databaseManager;
    }

    public void log(
            UUID sender,
            UUID target,
            double amount,
            TransactionType type,
            String reason
    ) throws Exception {

        Connection connection =
                databaseManager.getConnection();

        try (PreparedStatement statement =
                     connection.prepareStatement("""

                        INSERT INTO economy_transactions (
                            sender_uuid,
                            target_uuid,
                            amount,
                            transaction_type,
                            reason,
                            created_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?)

                    """)) {

            statement.setString(
                    1,
                    sender == null
                            ? null
                            : sender.toString()
            );

            statement.setString(
                    2,
                    target == null
                            ? null
                            : target.toString()
            );

            statement.setDouble(
                    3,
                    amount
            );

            statement.setString(
                    4,
                    type.name()
            );

            statement.setString(
                    5,
                    reason
            );

            statement.setLong(
                    6,
                    System.currentTimeMillis()
            );

            statement.executeUpdate();
        }
    }
}
