package org.voxelware.coretuff.Features.Moderation.PunishmentEngine;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class PunishmentIdGenerator {
	
	private static final String CHARS =
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	
	private static final SecureRandom RANDOM =
			new SecureRandom();
	
	private PunishmentIdGenerator() {}
	
	public static String generate(Connection connection) {
		
		while (true) {
			
			String id = randomId();
			
			if (!exists(connection, id)) {
				return id;
			}
		}
	}
	
	private static String randomId() {
		
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < 8; i++) {
			
			builder.append(
					CHARS.charAt(
							RANDOM.nextInt(CHARS.length())
					)
			);
		}
		
		return builder.toString();
	}
	
	private static boolean exists(Connection connection,
	                              String id) {
		
		try (PreparedStatement ps = connection.prepareStatement("""
            SELECT id
            FROM punishments
            WHERE id=?
        """)) {
			
			ps.setString(1, id);
			
			ResultSet rs = ps.executeQuery();
			
			return rs.next();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}
}