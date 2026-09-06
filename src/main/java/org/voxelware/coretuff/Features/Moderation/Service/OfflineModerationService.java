package org.voxelware.coretuff.Features.Moderation.Service;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.voxelware.coretuff.Features.Moderation.FormattEngine.BanFormatter;
import org.voxelware.coretuff.Features.Moderation.FormattEngine.BedrockBanFormatter;
import org.voxelware.coretuff.Features.Moderation.ModIO.BanCheckResult;
import org.voxelware.coretuff.Features.Moderation.PunishmentEngine.ModerationSummary;
import org.voxelware.coretuff.Features.Moderation.PunishmentEngine.ModeratorStat;
import org.voxelware.coretuff.Features.Moderation.PunishmentEngine.PagedResult;
import org.voxelware.coretuff.Features.Moderation.PunishmentEngine.PunishmentIdGenerator;
import org.voxelware.coretuff.Features.Moderation.PunishmentEngine.PunishmentRecord;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class OfflineModerationService implements ModerationService {

	private static final String TYPE_BAN = "BAN";
	private static final String TYPE_TEMPBAN = "TEMPBAN";
	private static final String TYPE_IPBAN = "IPBAN";

	private final Connection connection;

	public OfflineModerationService(Connection connection) {
		this.connection = connection;
		init();
	}

	private void init() {
		try (Statement st = connection.createStatement()) {
			st.execute("""
    CREATE TABLE IF NOT EXISTS punishments (

        id VARCHAR(16) PRIMARY KEY,

        uuid VARCHAR(64),
        name VARCHAR(16),
        ip VARCHAR(64),

        type VARCHAR(32) NOT NULL,

        reason VARCHAR(512),
        moderator VARCHAR(64),

        created_at BIGINT NOT NULL,
        expires_at BIGINT NOT NULL,

        active BOOLEAN DEFAULT TRUE,
        removed_at BIGINT DEFAULT -1,

        pardon_reason VARCHAR(512),
        pardoned_by VARCHAR(64)
    )
""");
			migrate(st);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	private void migrate(Statement st) {
		
		try {
			st.execute("""
            ALTER TABLE punishments
            ADD COLUMN active BOOLEAN DEFAULT TRUE
        """);
		} catch (SQLException ignored) {}
		
		try {
			st.execute("""
            ALTER TABLE punishments
            ADD COLUMN removed_at BIGINT DEFAULT -1
        """);
		} catch (SQLException ignored) {}
		
		try {
			st.execute("""
            ALTER TABLE punishments
            ADD COLUMN pardon_reason VARCHAR(512)
        """);
		} catch (SQLException ignored) {}
		
		try {
			st.execute("""
            ALTER TABLE punishments
            ADD COLUMN pardoned_by VARCHAR(64)
        """);
		} catch (SQLException ignored) {}
	}

	@Override
	public void ban(UUID uuid, String name, String reason, String moderator) {
		String id = save(uuid, name, null, TYPE_BAN, reason, moderator, -1L);
		Bukkit.getBanList(BanList.Type.NAME).addBan(name, reason, null, moderator);
		kickByName(name, buildBanMessage(uuid, reason, moderator, id, -1L));
	}

	@Override
	public void tempBan(UUID uuid, String name, String reason, long durationMillis, String moderator) {
		long expiresAt = System.currentTimeMillis() + durationMillis;
		String id = save(uuid, name, null, TYPE_TEMPBAN, reason, moderator, expiresAt);
		Bukkit.getBanList(BanList.Type.NAME).addBan(name, reason, new Date(expiresAt), moderator);
		kickByName(name, buildBanMessage(uuid, reason, moderator, id, expiresAt));
	}

	@Override
	public void unban(UUID uuid, String name) {
		deletePlayerPunishments(uuid, name);
		if (name != null) {
			Bukkit.getBanList(BanList.Type.NAME).pardon(name);
		}
	}

	@Override
	public BanCheckResult checkBan(UUID uuid, String name) {
		PunishmentRecord record = findLatestPlayerPunishment(uuid, name);
		if (record == null) {
			return new BanCheckResult(false, null);
		}

		if (isExpired(record.getExpiresAt())) {
			deletePunishmentById(
					record.getId(),
					"Punishment expired",
					"System"
			);
			if (record.getName() != null) {
				Bukkit.getBanList(BanList.Type.NAME).pardon(record.getName());
			}
			return new BanCheckResult(false, null);
		}
		
		return new BanCheckResult(true, buildBanMessage(
				uuid,
				record.getReason(),
				record.getModerator(),
				record.getId(),
				record.getExpiresAt()
		));
	}

	@Override
	public BanCheckResult checkIpBan(UUID uuid, String ip) {
		PunishmentRecord record = findLatestIpPunishment(ip);
		if (record == null) {
			return new BanCheckResult(false, null);
		}

		if (isExpired(record.getExpiresAt())) {
			deletePunishmentById(
					record.getId(),
					"Punishment expired",
					"System"
			);
			Bukkit.getBanList(BanList.Type.IP).pardon(ip);
			return new BanCheckResult(false, null);
		}
		
		return new BanCheckResult(true, buildBanMessage(
				uuid,
				record.getReason(),
				record.getModerator(),
				record.getId(),
				record.getExpiresAt()
		));
	}

	@Override
	public String getBanById(String id) {
		PunishmentRecord record = getPunishmentById(id);
		if (record == null) {
			return "No record found.";
		}
		
		return "Punishment #" + record.getId()
				+ "\nStatus: " + (record.isActive() ? "ACTIVE" : "PARDONED")
				+ "\nType: " + record.getType()
				+ "\nPlayer: " + record.getName()
				+ "\nModerator: " + record.getModerator()
				+ "\nReason: " + record.getReason()
				+ (
				record.isActive()
						? ""
						: "\nPardon Reason: " + record.getPardonReason()
						+ "\nPardoned By: " + record.getPardonedBy()
						+ "\nPardoned At: "
						+ new java.util.Date(record.getRemovedAt())
		);
	}

	@Override
	public PunishmentRecord getPunishmentById(String id) {
		try (PreparedStatement ps = connection.prepareStatement(
				"SELECT * FROM punishments WHERE id=?"
		)) {
			ps.setString(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return null;
				}
				return mapRecord(rs);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	@Override
	public void pardonById(String id,
	                       String pardonReason,
	                       String moderator) {
		
		PunishmentRecord record = getPunishmentById(id);
		
		if (record == null) {
			return;
		}
		
		deletePunishmentById(
				id,
				pardonReason,
				moderator
		);
		
		pardonMinecraftBan(record);
	}

	@Override
	public void pardonByPlayer(UUID uuid,
	                           String name,
	                           String pardonReason,
	                           String moderator) {
		List<PunishmentRecord> records = findPunishmentsForPlayer(uuid, name);
		for (PunishmentRecord record : records) {
			deletePunishmentById(
					record.getId(),
					pardonReason,
					moderator
			);
			pardonMinecraftBan(record);
		}

		if (records.isEmpty() && name != null) {
			Bukkit.getBanList(BanList.Type.NAME).pardon(name);
		}
	}

	@Override
	public void ipBan(String ip, String name, String reason, String moderator) {
		String id = save(null, name, ip, TYPE_IPBAN, reason, moderator, -1L);
		Bukkit.getBanList(BanList.Type.IP).addBan(ip, reason, null, moderator);
		kickByIp(ip, reason, moderator, id, -1L);
	}

	private String save(UUID uuid, String name, String ip, String type, String reason, String moderator, long expiresAt) {
		String id = generateId();
		try (PreparedStatement ps = connection.prepareStatement("""
			INSERT INTO punishments (id, uuid, ip, name, type, reason, moderator, expires_at, created_at)
			VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
		""")) {
			ps.setString(1, id);
			ps.setString(2, uuid == null ? null : uuid.toString());
			ps.setString(3, ip);
			ps.setString(4, normalizeName(name));
			ps.setString(5, type);
			ps.setString(6, reason);
			ps.setString(7, moderator);
			ps.setLong(8, expiresAt);
			ps.setLong(9, System.currentTimeMillis());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}

	private PunishmentRecord findLatestPlayerPunishment(UUID uuid, String name) {
		try (PreparedStatement ps = connection.prepareStatement("""
			SELECT * FROM punishments
			WHERE active=true
            AND type IN (?, ?)
			  AND (uuid = ? OR name = ?)
			ORDER BY created_at DESC
			LIMIT 1
		""")) {
			ps.setString(1, TYPE_BAN);
			ps.setString(2, TYPE_TEMPBAN);
			ps.setString(3, uuid == null ? null : uuid.toString());
			ps.setString(4, normalizeName(name));
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return null;
				}
				return mapRecord(rs);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private PunishmentRecord findLatestIpPunishment(String ip) {
		try (PreparedStatement ps = connection.prepareStatement("""
			SELECT * FROM punishments
			WHERE active=true
            AND type = ?
            AND ip = ?
			ORDER BY created_at DESC
			LIMIT 1
		""")) {
			ps.setString(1, TYPE_IPBAN);
			ps.setString(2, ip);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return null;
				}
				return mapRecord(rs);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private List<PunishmentRecord> findPunishmentsForPlayer(UUID uuid, String name) {
		List<PunishmentRecord> records = new ArrayList<>();
		try (PreparedStatement ps = connection.prepareStatement("""
			SELECT * FROM punishments
			WHERE active=true
            AND (uuid = ? OR name = ?)
		""")) {
			ps.setString(1, uuid == null ? null : uuid.toString());
			ps.setString(2, normalizeName(name));
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					records.add(mapRecord(rs));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return records;
	}
	
	private PunishmentRecord mapRecord(ResultSet rs) throws SQLException {
		
		return new PunishmentRecord(
				
				rs.getString("id"),
				
				rs.getString("uuid"),
				rs.getString("name"),
				
				rs.getString("type"),
				rs.getString("reason"),
				rs.getString("moderator"),
				
				rs.getLong("created_at"),
				rs.getLong("expires_at"),
				
				rs.getString("ip"),
				
				rs.getBoolean("active"),
				rs.getLong("removed_at"),
				
				rs.getString("pardon_reason"),
				rs.getString("pardoned_by")
		);
	}
	
	private void deletePunishmentById(String id,
	                                  String pardonReason,
	                                  String moderator) {
		
		try (PreparedStatement ps = connection.prepareStatement("""
        UPDATE punishments
        SET active=false,
            removed_at=?,
            pardon_reason=?,
            pardoned_by=?
        WHERE id=?
          AND active=true
    """)) {
			
			ps.setLong(1, System.currentTimeMillis());
			ps.setString(2, pardonReason);
			ps.setString(3, moderator);
			ps.setString(4, id);
			
			ps.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void deletePlayerPunishments(UUID uuid, String name) {
		
		try (PreparedStatement ps = connection.prepareStatement("""
        UPDATE punishments
        SET active=false,
            removed_at=?
        WHERE active=true
          AND type IN (?, ?)
          AND (uuid = ? OR name = ?)
    """)) {
			
			ps.setLong(1, System.currentTimeMillis());
			
			ps.setString(2, TYPE_BAN);
			ps.setString(3, TYPE_TEMPBAN);
			
			ps.setString(4, uuid == null ? null : uuid.toString());
			ps.setString(5, normalizeName(name));
			
			ps.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void pardonMinecraftBan(PunishmentRecord record) {
		if (TYPE_IPBAN.equals(record.getType()) && record.getIp() != null) {
			Bukkit.getBanList(BanList.Type.IP).pardon(record.getIp());
		}
		if (record.getName() != null) {
			Bukkit.getBanList(BanList.Type.NAME).pardon(record.getName());
		}
	}

	private void kickByName(String name, String message) {
		Player player = Bukkit.getPlayerExact(name);
		if (player != null) {
			player.kickPlayer(message);
		}
	}

	private void kickByIp(String ip, String message) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getAddress() == null) {
				continue;
			}
			String playerIp = player.getAddress().getAddress().getHostAddress();
			if (Objects.equals(playerIp, ip)) {
				player.kickPlayer(message);
			}
		}
	}

	private void kickByIp(String ip, String reason, String moderator, String id, long expiresAt) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getAddress() == null) {
				continue;
			}
			String playerIp = player.getAddress().getAddress().getHostAddress();
			if (Objects.equals(playerIp, ip)) {
				player.kickPlayer(buildBanMessage(player.getUniqueId(), reason, moderator, id, expiresAt));
			}
		}
	}

	private boolean isExpired(long expiresAt) {
		return expiresAt != -1L && System.currentTimeMillis() > expiresAt;
	}

	private String buildBanMessage(UUID uuid, String reason, String moderator, String id, long expiresAt) {
		if (isBedrockPlayer(uuid)) {
			return BedrockBanFormatter.format(reason, id);
		}

		return BanFormatter.format(reason, expiresAt, moderator, id);
	}
	
	private boolean isBedrockPlayer(UUID uuid) {
		
		if (uuid == null) {
			return false;
		}
		
		try {
			
			Class<?> apiClass =
					Class.forName("org.geysermc.floodgate.api.FloodgateApi");
			
			Method getInstance = apiClass.getMethod("getInstance");
			
			Object api = getInstance.invoke(null);
			
			Method isFloodgatePlayer =
					apiClass.getMethod("isFloodgatePlayer", UUID.class);
			
			Object result = isFloodgatePlayer.invoke(api, uuid);
			
			return result instanceof Boolean value && value
					|| isLikelyFloodgateUuid(uuid);
			
		} catch (Exception ignored) {
			
			return isLikelyFloodgateUuid(uuid);
		}
	}

	private boolean isLikelyFloodgateUuid(UUID uuid) {
		String value = uuid.toString();
		return value.startsWith("00000000-0000-0000-");
	}

	private String normalizeName(String name) {
		return name == null ? null : name.toLowerCase();
	}

	private String generateId() {
		return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}
	
	@Override
	public void syncVanillaBans() {
		
		for (BanEntry<?> entry :
				Bukkit.getBanList(BanList.Type.NAME).getEntries()) {
			
			try {
				
				String name = entry.getTarget();
				
				if (name == null) {
					continue;
				}
				
				if (hasVanillaImported(name)) {
					continue;
				}
				
				long expires = -1L;
				
				if (entry.getExpiration() != null) {
					expires = entry.getExpiration().getTime();
				}
				
				String type =
						expires == -1L
								? TYPE_BAN
								: TYPE_TEMPBAN;
				
				try (PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO punishments
                (
                    id,
                    uuid,
                    name,
                    ip,
                    type,
                    reason,
                    moderator,
                    created_at,
                    expires_at,
                    active,
                    removed_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """)) {
					
					ps.setString(1,
							PunishmentIdGenerator.generate(connection));
					
					ps.setString(2, null);
					
					ps.setString(3,
							normalizeName(name));
					
					ps.setString(4, null);
					
					ps.setString(5, type);
					
					ps.setString(6,
							entry.getReason());
					
					ps.setString(7,
							entry.getSource());
					
					ps.setLong(8,
							System.currentTimeMillis());
					
					ps.setLong(9, expires);
					
					ps.setBoolean(10, true);
					
					ps.setLong(11, -1L);
					
					ps.executeUpdate();
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		exportDatabaseBansToVanilla();
	}
	
	private void exportDatabaseBansToVanilla() {
		
		try (PreparedStatement ps = connection.prepareStatement("""
        SELECT *
        FROM punishments
        WHERE active=true
          AND type IN (?, ?)
    """)) {
			
			ps.setString(1, TYPE_BAN);
			ps.setString(2, TYPE_TEMPBAN);
			
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				
				String name = rs.getString("name");
				
				if (name == null) {
					continue;
				}
				
				long expires = rs.getLong("expires_at");
				
				Date expiration =
						expires == -1L
								? null
								: new Date(expires);
				
				Bukkit.getBanList(BanList.Type.NAME)
						.addBan(
								name,
								rs.getString("reason"),
								expiration,
								rs.getString("moderator")
						);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean hasVanillaImported(String name) {
		
		try (PreparedStatement ps = connection.prepareStatement("""
        SELECT id
        FROM punishments
        WHERE active=true
          AND name=?
          AND type IN (?, ?)
    """)) {
			
			ps.setString(1, normalizeName(name));
			
			ps.setString(2, TYPE_BAN);
			ps.setString(3, TYPE_TEMPBAN);
			
			ResultSet rs = ps.executeQuery();
			
			return rs.next();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	@Override
	public void syncVanillaIpBans() {
		
		for (BanEntry<?> entry :
				Bukkit.getBanList(BanList.Type.IP).getEntries()) {
			
			try {
				
				String ip = entry.getTarget();
				
				if (ip == null) {
					continue;
				}
				
				if (hasImportedIp(ip)) {
					continue;
				}
				
				long expires = -1L;
				
				if (entry.getExpiration() != null) {
					expires = entry.getExpiration().getTime();
				}
				
				try (PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO punishments
                (
                    id,
                    uuid,
                    name,
                    ip,
                    type,
                    reason,
                    moderator,
                    created_at,
                    expires_at,
                    active,
                    removed_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """)) {
					
					ps.setString(1,
							PunishmentIdGenerator.generate(connection));
					
					ps.setString(2, null);
					ps.setString(3, null);
					
					ps.setString(4, ip);
					
					ps.setString(5, TYPE_IPBAN);
					
					ps.setString(6,
							entry.getReason());
					
					ps.setString(7,
							entry.getSource());
					
					ps.setLong(8,
							System.currentTimeMillis());
					
					ps.setLong(9, expires);
					
					ps.setBoolean(10, true);
					
					ps.setLong(11, -1L);
					
					ps.executeUpdate();
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		exportDatabaseIpBans();
	}
	
	private void exportDatabaseIpBans() {
		
		try (PreparedStatement ps = connection.prepareStatement("""
        SELECT *
        FROM punishments
        WHERE active=true
          AND type=?
    """)) {
			
			ps.setString(1, TYPE_IPBAN);
			
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				
				String ip = rs.getString("ip");
				
				if (ip == null) {
					continue;
				}
				
				long expires = rs.getLong("expires_at");
				
				Date expiration =
						expires == -1L
								? null
								: new Date(expires);
				
				Bukkit.getBanList(BanList.Type.IP)
						.addBan(
								ip,
								rs.getString("reason"),
								expiration,
								rs.getString("moderator")
						);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private boolean hasImportedIp(String ip) {
		
		try (PreparedStatement ps = connection.prepareStatement("""
        SELECT id
        FROM punishments
        WHERE active=true
          AND ip=?
          AND type=?
    """)) {
			
			ps.setString(1, ip);
			ps.setString(2, TYPE_IPBAN);
			
			ResultSet rs = ps.executeQuery();
			
			return rs.next();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	@Override
	public List<PunishmentRecord> getRecentPunishments(int limit) {
		
		List<PunishmentRecord> list = new ArrayList<>();
		
		try (PreparedStatement ps = connection.prepareStatement("""
        SELECT *
        FROM punishments
        ORDER BY created_at DESC
        LIMIT ?
    """)) {
			
			ps.setInt(1, limit);
			
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				list.add(mapRecord(rs));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return list;
	}

	@Override
	public PagedResult<PunishmentRecord> getPunishmentsPaginated(
			int page, int pageSize, String search,
			String typeFilter, String statusFilter,
			String sortField, String sortOrder) {

		List<Object> params = new ArrayList<>();
		StringBuilder where = new StringBuilder(" WHERE 1=1");

		if (search != null && !search.isBlank()) {
			String term = "%" + search.toLowerCase() + "%";
			where.append(" AND (LOWER(id) LIKE ? OR LOWER(name) LIKE ? OR LOWER(uuid) LIKE ? OR LOWER(moderator) LIKE ? OR LOWER(reason) LIKE ?)");
			for (int i = 0; i < 5; i++) params.add(term);
		}

		if (typeFilter != null && !typeFilter.isBlank() && !"ALL".equalsIgnoreCase(typeFilter)) {
			where.append(" AND type = ?");
			params.add(typeFilter.toUpperCase());
		}

		long now = System.currentTimeMillis();
		if (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter)) {
			switch (statusFilter.toLowerCase()) {
				case "active" -> where.append(" AND active = true AND (expires_at = -1 OR expires_at > ?)");
				case "pardoned" -> where.append(" AND active = false");
				case "expired" -> where.append(" AND active = true AND expires_at != -1 AND expires_at <= ?");
				default -> {}
			}
			if ("active".equalsIgnoreCase(statusFilter) || "expired".equalsIgnoreCase(statusFilter)) {
				params.add(now);
			}
		}

		String safeSort = switch (sortField != null ? sortField.toLowerCase() : "") {
			case "id", "name", "moderator", "type", "created_at", "expires_at" -> sortField.toLowerCase();
			default -> "created_at";
		};
		String safeOrder = "asc".equalsIgnoreCase(sortOrder) ? "ASC" : "DESC";

		String countSql = "SELECT COUNT(*) FROM punishments" + where;
		int total = 0;
		try (PreparedStatement ps = connection.prepareStatement(countSql)) {
			for (int i = 0; i < params.size(); i++) {
				ps.setObject(i + 1, params.get(i));
			}
			ResultSet rs = ps.executeQuery();
			if (rs.next()) total = rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		int offset = (Math.max(1, page) - 1) * pageSize;
		String dataSql = "SELECT * FROM punishments" + where + " ORDER BY " + safeSort + " " + safeOrder + " LIMIT ? OFFSET ?";
		List<PunishmentRecord> items = new ArrayList<>();
		try (PreparedStatement ps = connection.prepareStatement(dataSql)) {
			for (int i = 0; i < params.size(); i++) {
				ps.setObject(i + 1, params.get(i));
			}
			ps.setInt(params.size() + 1, pageSize);
			ps.setInt(params.size() + 2, offset);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				items.add(mapRecord(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return new PagedResult<>(items, total, page, pageSize);
	}

	@Override
	public ModerationSummary getSummary() {
		int total = 0, active = 0, bans = 0, tempBans = 0, ipBans = 0, pardoned = 0, expired = 0, thisWeek = 0, thisMonth = 0;
		long now = System.currentTimeMillis();
		long weekAgo = now - 7L * 24 * 60 * 60 * 1000;
		long monthAgo = now - 30L * 24 * 60 * 60 * 1000;

		try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM punishments")) {
			ResultSet rs = ps.executeQuery();
			if (rs.next()) total = rs.getInt(1);
		} catch (SQLException e) { e.printStackTrace(); }

		try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM punishments WHERE active=true")) {
			ResultSet rs = ps.executeQuery();
			if (rs.next()) active = rs.getInt(1);
		} catch (SQLException e) { e.printStackTrace(); }

		try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM punishments WHERE type=?")) {
			ps.setString(1, TYPE_BAN);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) bans = rs.getInt(1);
		} catch (SQLException e) { e.printStackTrace(); }

		try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM punishments WHERE type=?")) {
			ps.setString(1, TYPE_TEMPBAN);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) tempBans = rs.getInt(1);
		} catch (SQLException e) { e.printStackTrace(); }

		try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM punishments WHERE type=?")) {
			ps.setString(1, TYPE_IPBAN);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) ipBans = rs.getInt(1);
		} catch (SQLException e) { e.printStackTrace(); }

		try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM punishments WHERE active=false")) {
			ResultSet rs = ps.executeQuery();
			if (rs.next()) pardoned = rs.getInt(1);
		} catch (SQLException e) { e.printStackTrace(); }

		try (PreparedStatement ps = connection.prepareStatement(
				"SELECT COUNT(*) FROM punishments WHERE active=true AND expires_at != -1 AND expires_at <= ?")) {
			ps.setLong(1, now);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) expired = rs.getInt(1);
		} catch (SQLException e) { e.printStackTrace(); }

		try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM punishments WHERE created_at >= ?")) {
			ps.setLong(1, weekAgo);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) thisWeek = rs.getInt(1);
		} catch (SQLException e) { e.printStackTrace(); }

		try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM punishments WHERE created_at >= ?")) {
			ps.setLong(1, monthAgo);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) thisMonth = rs.getInt(1);
		} catch (SQLException e) { e.printStackTrace(); }

		return new ModerationSummary(total, active, bans, tempBans, ipBans, pardoned, expired, thisWeek, thisMonth);
	}

	@Override
	public List<ModeratorStat> getModeratorStats() {
		List<ModeratorStat> stats = new ArrayList<>();
		try (PreparedStatement ps = connection.prepareStatement(
				"SELECT moderator, COUNT(*) as cnt FROM punishments WHERE moderator IS NOT NULL GROUP BY moderator ORDER BY cnt DESC LIMIT 10")) {
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				stats.add(new ModeratorStat(rs.getString("moderator"), rs.getInt("cnt")));
			}
		} catch (SQLException e) { e.printStackTrace(); }
		return stats;
	}

	@Override
	public PagedResult<PunishmentRecord> getPlayerHistory(String uuid, int page, int pageSize) {
		int total = 0;
		try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM punishments WHERE uuid = ?")) {
			ps.setString(1, uuid);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) total = rs.getInt(1);
		} catch (SQLException e) { e.printStackTrace(); }

		int offset = (Math.max(1, page) - 1) * pageSize;
		List<PunishmentRecord> items = new ArrayList<>();
		try (PreparedStatement ps = connection.prepareStatement(
				"SELECT * FROM punishments WHERE uuid = ? ORDER BY created_at DESC LIMIT ? OFFSET ?")) {
			ps.setString(1, uuid);
			ps.setInt(2, pageSize);
			ps.setInt(3, offset);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				items.add(mapRecord(rs));
			}
		} catch (SQLException e) { e.printStackTrace(); }

		return new PagedResult<>(items, total, page, pageSize);
	}
}
