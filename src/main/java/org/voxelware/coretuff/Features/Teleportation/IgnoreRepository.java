package org.voxelware.coretuff.Features.Teleportation;

import java.sql.*;
import java.util.*;

public class IgnoreRepository {
	
	private final Connection connection;
	
	public IgnoreRepository(Connection connection) {
		this.connection = connection;
	}
	public void addIgnore(UUID player, UUID target) {
		try (PreparedStatement ps = connection.prepareStatement(
				"INSERT INTO tp_ignore (player_uuid, ignored_uuid) VALUES (?, ?)"
		)) {
			ps.setString(1, player.toString());
			ps.setString(2, target.toString());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void removeIgnore(UUID player, UUID target) {
		try (PreparedStatement ps = connection.prepareStatement(
				"DELETE FROM tp_ignore WHERE player_uuid=? AND ignored_uuid=?"
		)) {
			ps.setString(1, player.toString());
			ps.setString(2, target.toString());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public Set<UUID> getIgnored(UUID player) {
		Set<UUID> set = new HashSet<>();
		
		try (PreparedStatement ps = connection.prepareStatement(
				"SELECT ignored_uuid FROM tp_ignore WHERE player_uuid=?"
		)) {
			ps.setString(1, player.toString());
			
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					set.add(UUID.fromString(rs.getString("ignored_uuid")));
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return set;
	}
	public boolean isIgnoring(UUID player, UUID target) {
		try (PreparedStatement ps = connection.prepareStatement(
				"SELECT 1 FROM tp_ignore WHERE player_uuid=? AND ignored_uuid=?"
		)) {
			ps.setString(1, player.toString());
			ps.setString(2, target.toString());
			
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	public Map<UUID, Set<UUID>> loadAll() {
		Map<UUID, Set<UUID>> map = new HashMap<>();
		
		try (PreparedStatement ps = connection.prepareStatement(
				"SELECT player_uuid, ignored_uuid FROM tp_ignore"
		);
		     ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				UUID player = UUID.fromString(rs.getString("player_uuid"));
				UUID ignored = UUID.fromString(rs.getString("ignored_uuid"));
				
				map.computeIfAbsent(player, k -> new HashSet<>()).add(ignored);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return map;
	}
}