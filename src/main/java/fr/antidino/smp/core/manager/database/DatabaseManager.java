package fr.antidino.smp.core.manager.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final Plugin plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(Plugin plugin) {
        this.plugin = plugin;
    }


    public void initDatabase() {
        HikariConfig config = new HikariConfig();


        File dbFile = new File(plugin.getDataFolder(), "database.db");
        if (!dbFile.exists()) {
            try {
                dbFile.getParentFile().mkdirs();
                dbFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Impossible de créer le fichier de base de données !");
                e.printStackTrace();
            }
        }

        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setMaximumPoolSize(10); // Nombre max de connexions simultanées
        config.setPoolName("SMP-HikariPool");


        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);


        createTables();
    }


    private void createTables() {
        String playersTable = """
            CREATE TABLE IF NOT EXISTS smp_players (
                uuid VARCHAR(36) PRIMARY KEY,
                username VARCHAR(16)
            );
        """;

        String perksTable = """
                CREATE TABLE IF NOT EXISTS smp_player_perks (
                    uuid VARCHAR(36),
                    perk_id VARCHAR(64),
                    created_at DATETIME,
                    PRIMARY KEY (uuid, perk_id)
                );
        """;

        // Exécution des requêtes de création
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(playersTable);
            stmt.execute(perksTable);
            plugin.getLogger().info("Tables de la base de données initialisées avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Récupérer une connexion depuis le pool Hikari
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    // Fermer proprement le pool à l'arrêt du plugin
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}