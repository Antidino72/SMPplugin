package fr.antidino.smp.core.database;

import fr.antidino.smp.CorePlugin;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;



public class DatabaseProvider {
    private static final Logger log = LoggerFactory.getLogger(DatabaseProvider.class);
    CorePlugin plugin;
    DatabaseManager databaseManager;
    private static final String INSERT_PLAYER_SQL = """
            INSERT OR IGNORE INTO smp_players (uuid, username) VALUES (?, ?)
    """;
    private static  final  String GET_PLAYER_SQL  = """
            SELECT * FROM smp_players WHERE uuid=?
    """;
    private static final String INSERT_PERK_CRAFT_SQL = """
        INSERT INTO smp_player_perks (uuid, perk_id, created_at) VALUES (?, ?, ?);
    """;

    private static String GET_PERK_CRAFT_SQL = """
            SELECT * FROM smp_player_perks WHERE perk_id=?
    """;
    public DatabaseProvider(CorePlugin plugin) {
        this.plugin = plugin;
        databaseManager = new DatabaseManager(plugin);
        databaseManager.initDatabase();
    }
    public boolean hasEntry(String uuid) {
        try (Connection connection = getConnection()){
            PreparedStatement ps  = connection.prepareStatement(GET_PLAYER_SQL);
            ps.setString(1, uuid);
            return ps.executeQuery().next();
        }catch (SQLException e){
            ShowSqlError(e);
            return false;
        }
    }
    public void insertPlayerSQL(Player player) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_PLAYER_SQL)) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, player.getName());
            ps.executeUpdate();


        } catch (SQLException e) {
            ShowSqlError(e);
        }
    }
    public void InsertPerkCraftSQL(String perk_id,String uuid){
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_PERK_CRAFT_SQL)) {
            ps.setString(1, uuid);
            ps.setString(2, perk_id);
            ps.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            ps.executeUpdate();
        } catch (SQLException e) {
            ShowSqlError(e);
        }
    }

    public int getNumberOfCraft(String perk_id){
        try (Connection connection = getConnection()){
            PreparedStatement ps = connection.prepareStatement(GET_PERK_CRAFT_SQL);
            ps.setString(1, perk_id);
            ResultSet rs = ps.executeQuery();
            int count = 0;
            while (rs.next()) {
                count++;
            }
            return count;
        }catch (SQLException e){
            ShowSqlError(e);
        }
        return 3;
    };
    
    /** Close Connexion**/
    public void CloseConnection(){
        if (databaseManager == null) return;
        try (Connection connection = getConnection()){
            databaseManager.close();
        }catch (SQLException e){
            ShowSqlError(e);
        }
    }
    
    //util
    private Connection getConnection() throws SQLException {
        return databaseManager.getConnection();
    }
    private void ShowSqlError(SQLException e) {
        log.info(" Error as {}", String.valueOf(e));
    }
}
