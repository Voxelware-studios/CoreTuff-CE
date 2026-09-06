package org.voxelware.coretuff.Features.Lands.Homes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.voxelware.coretuff.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HomeRepository {

    private static final String TABLE = "homes";
    private static final String SELECT_COLS = "home_name, world, x, y, z, yaw, pitch";

    private final DatabaseManager databaseManager;

    public HomeRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void setHome(UUID uuid, String name, Location location) throws Exception {
        Connection connection = databaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement("""
            MERGE INTO %s (player_uuid, home_name, world, x, y, z, yaw, pitch)
            KEY(player_uuid, home_name)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """.formatted(TABLE))) {
            statement.setString(1, uuid.toString());
            statement.setString(2, name.toLowerCase());
            statement.setString(3, location.getWorld().getName());
            statement.setDouble(4, location.getX());
            statement.setDouble(5, location.getY());
            statement.setDouble(6, location.getZ());
            statement.setFloat(7, location.getYaw());
            statement.setFloat(8, location.getPitch());
            statement.executeUpdate();
        }
    }

    public Home getHome(UUID uuid, String name) throws Exception {
        Connection connection = databaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT %s FROM %s
            WHERE player_uuid = ? AND home_name = ?
        """.formatted(SELECT_COLS, TABLE))) {
            statement.setString(1, uuid.toString());
            statement.setString(2, name.toLowerCase());
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) return null;
                return mapHome(rs);
            }
        }
    }

    public boolean deleteHome(UUID uuid, String name) throws Exception {
        Connection connection = databaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement("""
            DELETE FROM %s
            WHERE player_uuid = ? AND home_name = ?
        """.formatted(TABLE))) {
            statement.setString(1, uuid.toString());
            statement.setString(2, name.toLowerCase());
            return statement.executeUpdate() > 0;
        }
    }

    public List<Home> getHomes(UUID uuid) throws Exception {
        Connection connection = databaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT %s FROM %s
            WHERE player_uuid = ?
            ORDER BY home_name ASC
        """.formatted(SELECT_COLS, TABLE))) {
            statement.setString(1, uuid.toString());
            try (ResultSet rs = statement.executeQuery()) {
                List<Home> homes = new ArrayList<>();
                while (rs.next()) {
                    homes.add(mapHome(rs));
                }
                return homes;
            }
        }
    }

    public int getHomeCount(UUID uuid) throws Exception {
        Connection connection = databaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT COUNT(*) FROM %s WHERE player_uuid = ?
        """.formatted(TABLE))) {
            statement.setString(1, uuid.toString());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Home mapHome(ResultSet rs) throws Exception {
        return new Home(
                rs.getString("home_name"),
                rs.getString("world"),
                rs.getDouble("x"),
                rs.getDouble("y"),
                rs.getDouble("z"),
                rs.getFloat("yaw"),
                rs.getFloat("pitch")
        );
    }

    public Location toLocation(Home home) {
        World world = Bukkit.getWorld(home.getWorld());
        if (world == null) return null;
        return new Location(world, home.getX(), home.getY(), home.getZ(), home.getYaw(), home.getPitch());
    }
}
