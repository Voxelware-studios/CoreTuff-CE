package org.voxelware.coretuff.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
	
	private Connection connection;
	
	public void connect(JavaPlugin plugin) throws SQLException {
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			throw new SQLException("H2 Driver not found!", e);
		}
		if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();

		String url = "jdbc:h2:" + new File(plugin.getDataFolder(), "database").getAbsolutePath()
				+ ";TRACE_LEVEL_FILE=0";
		
		connection = DriverManager.getConnection(url);
		
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("""
     
			  CREATE TABLE IF NOT EXISTS tp_ignore (
               player_uuid VARCHAR(36),
               ignored_uuid VARCHAR(36),
               UNIQUE(player_uuid, ignored_uuid)
           );
            """);

			stmt.execute("""

    CREATE TABLE IF NOT EXISTS homes (
        player_uuid VARCHAR(36),
        home_name VARCHAR(32),
        world VARCHAR(100),
        x DOUBLE,
        y DOUBLE,
        z DOUBLE,
        yaw FLOAT,
        pitch FLOAT,
        PRIMARY KEY(player_uuid, home_name)
    );

""");

			migrateHomes(stmt);

			stmt.execute("""

    CREATE TABLE IF NOT EXISTS warps (
        name VARCHAR(32) PRIMARY KEY,
        world VARCHAR(100),
        x DOUBLE,
        y DOUBLE,
        z DOUBLE,
        yaw FLOAT,
        pitch FLOAT,
        owner_uuid VARCHAR(36),
        description VARCHAR(255),
        created_at BIGINT
    );

""");

			stmt.execute("""

    CREATE TABLE IF NOT EXISTS economy (
        player_uuid VARCHAR(36) PRIMARY KEY,
        balance DOUBLE
    );

""");

			stmt.execute("""

    CREATE TABLE IF NOT EXISTS economy_transactions (
        id IDENTITY PRIMARY KEY,
        sender_uuid VARCHAR(36),
        target_uuid VARCHAR(36),
        amount DOUBLE,
        transaction_type VARCHAR(50),
        reason VARCHAR(255),
        created_at BIGINT
    );

""");

			stmt.execute("""

    CREATE TABLE IF NOT EXISTS jails (
        name VARCHAR(32) PRIMARY KEY,
        world VARCHAR(100),
        x DOUBLE,
        y DOUBLE,
        z DOUBLE,
        yaw FLOAT,
        pitch FLOAT
    );

""");

			stmt.execute("""

    CREATE TABLE IF NOT EXISTS jailed_players (
        player_uuid VARCHAR(36) PRIMARY KEY,
        jail_name VARCHAR(32),
        jailed_by VARCHAR(36),
        reason VARCHAR(255),
        jailed_at BIGINT,
        release_at BIGINT
    );

""");
		}


	}
	
	private void migrateHomes(Statement stmt) {
		try {
			stmt.execute("ALTER TABLE homes DROP PRIMARY KEY");
		} catch (SQLException ignored) {}
		try {
			stmt.execute("ALTER TABLE homes ALTER COLUMN home_id RENAME TO home_name");
		} catch (SQLException ignored) {}
		try {
			stmt.execute("ALTER TABLE homes ALTER COLUMN home_name VARCHAR(32)");
		} catch (SQLException ignored) {}
		try {
			stmt.execute("ALTER TABLE homes ADD PRIMARY KEY(player_uuid, home_name)");
		} catch (SQLException ignored) {}
	}
	public Connection getConnection() {
		return connection;
	}
}
