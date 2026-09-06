package org.voxelware.coretuff.Features.Moderation.Jail;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.voxelware.coretuff.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JailRepository {

    private final Connection connection;

    public JailRepository(DatabaseManager db) {
        this.connection = db.getConnection();
    }

    public void setJail(Jail jail) throws Exception {
        try (PreparedStatement stmt = connection.prepareStatement(
                "MERGE INTO jails (name, world, x, y, z, yaw, pitch) KEY(name) VALUES(?,?,?,?,?,?,?)")) {
            stmt.setString(1, jail.getName());
            stmt.setString(2, jail.getWorld());
            stmt.setDouble(3, jail.getX());
            stmt.setDouble(4, jail.getY());
            stmt.setDouble(5, jail.getZ());
            stmt.setFloat(6, jail.getYaw());
            stmt.setFloat(7, jail.getPitch());
            stmt.executeUpdate();
        }
    }

    public boolean deleteJail(String name) throws Exception {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM jails WHERE name=?")) {
            stmt.setString(1, name);
            return stmt.executeUpdate() > 0;
        }
    }

    public Jail getJail(String name) throws Exception {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM jails WHERE name=?")) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    public List<Jail> getAllJails() throws Exception {
        List<Jail> jails = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM jails")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    jails.add(map(rs));
                }
            }
        }
        return jails;
    }

    public Location toLocation(Jail jail) {
        var world = Bukkit.getWorld(jail.getWorld());
        if (world == null) return null;
        return new Location(world, jail.getX(), jail.getY(), jail.getZ(), jail.getYaw(), jail.getPitch());
    }

    private Jail map(ResultSet rs) throws Exception {
        return new Jail(
                rs.getString("name"),
                rs.getString("world"),
                rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                rs.getFloat("yaw"), rs.getFloat("pitch")
        );
    }

    public void jailPlayer(UUID playerUuid, String jailName, String by, String reason, long until) throws Exception {
        try (PreparedStatement stmt = connection.prepareStatement(
                "MERGE INTO jailed_players (player_uuid, jail_name, jailed_by, reason, jailed_at, release_at) KEY(player_uuid) VALUES(?,?,?,?,?,?)")) {
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, jailName);
            stmt.setString(3, by);
            stmt.setString(4, reason);
            stmt.setLong(5, System.currentTimeMillis());
            stmt.setLong(6, until);
            stmt.executeUpdate();
        }
    }

    public void unjailPlayer(UUID playerUuid) throws Exception {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM jailed_players WHERE player_uuid=?")) {
            stmt.setString(1, playerUuid.toString());
            stmt.executeUpdate();
        }
    }

    public boolean isPlayerJailed(UUID playerUuid) throws Exception {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT 1 FROM jailed_players WHERE player_uuid=?")) {
            stmt.setString(1, playerUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public JailedPlayer getJailedPlayer(UUID playerUuid) throws Exception {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM jailed_players WHERE player_uuid=?")) {
            stmt.setString(1, playerUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new JailedPlayer(
                            UUID.fromString(rs.getString("player_uuid")),
                            rs.getString("jail_name"),
                            rs.getString("jailed_by"),
                            rs.getString("reason"),
                            rs.getLong("jailed_at"),
                            rs.getLong("release_at")
                    );
                }
            }
        }
        return null;
    }

    public List<JailedPlayer> getAllJailedPlayers() throws Exception {
        List<JailedPlayer> list = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM jailed_players")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new JailedPlayer(
                            UUID.fromString(rs.getString("player_uuid")),
                            rs.getString("jail_name"),
                            rs.getString("jailed_by"),
                            rs.getString("reason"),
                            rs.getLong("jailed_at"),
                            rs.getLong("release_at")
                    ));
                }
            }
        }
        return list;
    }

    public static class JailedPlayer {
        private final UUID playerUuid;
        private final String jailName;
        private final String jailedBy;
        private final String reason;
        private final long jailedAt;
        private final long releaseAt;

        public JailedPlayer(UUID playerUuid, String jailName, String jailedBy, String reason, long jailedAt, long releaseAt) {
            this.playerUuid = playerUuid;
            this.jailName = jailName;
            this.jailedBy = jailedBy;
            this.reason = reason;
            this.jailedAt = jailedAt;
            this.releaseAt = releaseAt;
        }

        public UUID getPlayerUuid() { return playerUuid; }
        public String getJailName() { return jailName; }
        public String getJailedBy() { return jailedBy; }
        public String getReason() { return reason; }
        public long getJailedAt() { return jailedAt; }
        public long getReleaseAt() { return releaseAt; }
    }
}
