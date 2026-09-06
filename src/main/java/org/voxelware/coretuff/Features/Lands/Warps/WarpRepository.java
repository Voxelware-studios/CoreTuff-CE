package org.voxelware.coretuff.Features.Lands.Warps;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.voxelware.coretuff.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class WarpRepository {

    private static final String TABLE = "warps";
    private static final String COLS = "name, world, x, y, z, yaw, pitch, owner_uuid, description, created_at";

    private final DatabaseManager databaseManager;

    public WarpRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void setWarp(Warp warp) throws Exception {
        Connection connection = databaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement("""
            MERGE INTO %s (%s)
            KEY(name)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.formatted(TABLE, COLS))) {
            statement.setString(1, warp.getName().toLowerCase());
            statement.setString(2, warp.getWorld());
            statement.setDouble(3, warp.getX());
            statement.setDouble(4, warp.getY());
            statement.setDouble(5, warp.getZ());
            statement.setFloat(6, warp.getYaw());
            statement.setFloat(7, warp.getPitch());
            statement.setString(8, warp.getOwnerUuid());
            statement.setString(9, warp.getDescription());
            statement.setLong(10, warp.getCreatedAt());
            statement.executeUpdate();
        }
    }

    public Warp getWarp(String name) throws Exception {
        Connection connection = databaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT %s FROM %s WHERE name = ?
        """.formatted(COLS, TABLE))) {
            statement.setString(1, name.toLowerCase());
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) return null;
                return mapWarp(rs);
            }
        }
    }

    public boolean deleteWarp(String name) throws Exception {
        Connection connection = databaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement("""
            DELETE FROM %s WHERE name = ?
        """.formatted(TABLE))) {
            statement.setString(1, name.toLowerCase());
            return statement.executeUpdate() > 0;
        }
    }

    public List<Warp> getAllWarps() throws Exception {
        Connection connection = databaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT %s FROM %s ORDER BY name ASC
        """.formatted(COLS, TABLE))) {
            try (ResultSet rs = statement.executeQuery()) {
                List<Warp> warps = new ArrayList<>();
                while (rs.next()) {
                    warps.add(mapWarp(rs));
                }
                return warps;
            }
        }
    }

    public int getWarpCount(String ownerUuid) throws Exception {
        Connection connection = databaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT COUNT(*) FROM %s WHERE owner_uuid = ?
        """.formatted(TABLE))) {
            statement.setString(1, ownerUuid);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public boolean exists(String name) throws Exception {
        Connection connection = databaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT 1 FROM %s WHERE name = ?
        """.formatted(TABLE))) {
            statement.setString(1, name.toLowerCase());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Warp mapWarp(ResultSet rs) throws Exception {
        return new Warp(
                rs.getString("name"),
                rs.getString("world"),
                rs.getDouble("x"),
                rs.getDouble("y"),
                rs.getDouble("z"),
                rs.getFloat("yaw"),
                rs.getFloat("pitch"),
                rs.getString("owner_uuid"),
                rs.getString("description"),
                rs.getLong("created_at")
        );
    }

    public Location toLocation(Warp warp) {
        World world = Bukkit.getWorld(warp.getWorld());
        if (world == null) return null;
        return new Location(world, warp.getX(), warp.getY(), warp.getZ(), warp.getYaw(), warp.getPitch());
    }
}
