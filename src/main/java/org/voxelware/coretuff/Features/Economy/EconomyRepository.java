package org.voxelware.coretuff.Features.Economy;

import org.voxelware.coretuff.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EconomyRepository {

    private final DatabaseManager databaseManager;

    public EconomyRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        migrate();
    }

    private void migrate() {
        try (Statement st = databaseManager.getConnection().createStatement()) {
            st.execute("""
                ALTER TABLE economy
                ADD COLUMN last_interest_at BIGINT DEFAULT 0
            """);
        } catch (Exception ignored) {
        }
    }

    public double getBalance(UUID uuid) throws Exception {
        Connection connection = databaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT balance, last_interest_at
            FROM economy
            WHERE player_uuid = ?
        """)) {
            statement.setString(1, uuid.toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    createAccount(uuid);
                    return 0;
                }
                return rs.getDouble("balance");
            }
        }
    }

    public long getLastInterestAt(UUID uuid) throws Exception {
        Connection connection = databaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT last_interest_at
            FROM economy
            WHERE player_uuid = ?
        """)) {
            statement.setString(1, uuid.toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return 0L;
                }
                return rs.getLong("last_interest_at");
            }
        }
    }

    public void setLastInterestAt(UUID uuid, long timestamp) throws Exception {
        Connection connection = databaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement("""
            UPDATE economy SET last_interest_at = ? WHERE player_uuid = ?
        """)) {
            statement.setLong(1, timestamp);
            statement.setString(2, uuid.toString());
            statement.executeUpdate();
        }
    }

    public void setBalance(UUID uuid, double amount) throws Exception {
        Connection connection = databaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement("""
            MERGE INTO economy (player_uuid, balance)
            KEY(player_uuid)
            VALUES (?, ?)
        """)) {
            statement.setString(1, uuid.toString());
            statement.setDouble(2, amount);
            statement.executeUpdate();
        }
    }

    public void createAccount(UUID uuid) throws Exception {
        setBalance(uuid, 0);
    }

    public List<Map.Entry<UUID, Double>> getAllBalancesSorted() throws Exception {
        List<Map.Entry<UUID, Double>> results = new ArrayList<>();
        Connection connection = databaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT player_uuid, balance
            FROM economy
            ORDER BY balance DESC
        """)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("player_uuid"));
                    double balance = rs.getDouble("balance");
                    results.add(new AbstractMap.SimpleEntry<>(uuid, balance));
                }
            }
        }
        return results;
    }

    public List<Map.Entry<UUID, Double>> getTopBalances(int limit) throws Exception {
        List<Map.Entry<UUID, Double>> results = new ArrayList<>();
        Connection connection = databaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT player_uuid, balance
            FROM economy
            ORDER BY balance DESC
            LIMIT ?
        """)) {
            statement.setInt(1, limit);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("player_uuid"));
                    double balance = rs.getDouble("balance");
                    results.add(new AbstractMap.SimpleEntry<>(uuid, balance));
                }
            }
        }
        return results;
    }
}
