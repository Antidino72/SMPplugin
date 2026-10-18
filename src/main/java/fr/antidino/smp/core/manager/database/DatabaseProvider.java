package fr.antidino.smp.core.manager.database;

import fr.antidino.smp.CorePlugin;
import org.bukkit.entity.Player;

import java.sql.*;

public class DatabaseProvider {
    CorePlugin plugin;

    private static final String INSERT_PLAYER_SQL = """
            INSERT OR IGNORE INTO smp_players (uuid, username) VALUES (?, ?)
    """;
    private static  final  String GET_PLAYER_SQL  = """
            SELECT * FROM smp_players WHERE uuid=?
    """;
    private static final String INSERT_PERK_CRAFT_SQL = """
        INSERT INTO smp_player_perks (uuid, perk_id, created_at) VALUES (?, ?, ?);
    """;
    public DatabaseProvider(CorePlugin plugin) {
        this.plugin = plugin;
    }
    public boolean hasEntry(String uuid) {
        try (Connection connection = plugin.getConnexion()){
            PreparedStatement ps  = connection.prepareStatement(GET_PLAYER_SQL);
            ps.setString(1, uuid);
            return ps.executeQuery().next();
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    public void insertPlayerSQL(Player player) {
        try (Connection conn = plugin.getConnexion();
             PreparedStatement ps = conn.prepareStatement(INSERT_PLAYER_SQL)) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, player.getName());
            ps.executeUpdate();


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void InsertPerkCraftSQL(String perk_id,String uuid){
        try (Connection conn = plugin.getConnexion();
             PreparedStatement ps = conn.prepareStatement(INSERT_PERK_CRAFT_SQL)) {
            ps.setString(1, uuid);
            ps.setString(2, perk_id);
            ps.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
