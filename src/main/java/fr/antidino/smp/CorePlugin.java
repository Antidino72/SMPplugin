package fr.antidino.smp;

import fr.antidino.smp.core.manager.SlotManager;
import fr.antidino.smp.core.manager.codex.CodexListener;
import fr.antidino.smp.core.manager.codex.CodexMenu;
import fr.antidino.smp.core.commands.AdminCommands;
import fr.antidino.smp.core.commands.PerksCommand;
import fr.antidino.smp.core.manager.database.DatabaseManager;
import fr.antidino.smp.core.manager.database.DatabaseProvider;
import fr.antidino.smp.core.listenner.CraftingListener;
import fr.antidino.smp.core.listenner.ItemBreakListener;
import fr.antidino.smp.core.listenner.PlayerJoinListener;
import fr.antidino.smp.core.listenner.SmithingListener;
import fr.antidino.smp.core.SmithingRegisterAllItem;
import fr.antidino.smp.core.manager.perk.config.ConfigPerkManager;
import fr.antidino.smp.core.manager.perk.PerkManager;
import fr.antidino.smp.librairie.PDCBuilder;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public final class CorePlugin extends JavaPlugin {
    private static final Logger log = LoggerFactory.getLogger(CorePlugin.class);
    public PDCBuilder pdcBuilder;
    public PerkManager perkManager;
    public SlotManager slotManager;
    public ConfigPerkManager configPerkManager;
    public CodexMenu codexMenu;

    private DatabaseManager databaseManager;
    public DatabaseProvider databaseProvider;
    @Override
    public void onEnable() {
        configPerkManager = new ConfigPerkManager(this);
        initConfig();

        getServer().getPluginManager().registerEvents(new CraftingListener(this), this);
        getServer().getPluginManager().registerEvents(new SmithingListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        pdcBuilder = new PDCBuilder(this);

        PDCBuilder.init(this);
        perkManager = new PerkManager(this);
        slotManager = new SlotManager(this);
        new SmithingRegisterAllItem(this);
        getCommand("perk").setExecutor(new AdminCommands(this));

        codexMenu = new CodexMenu(this);
        getServer().getPluginManager().registerEvents(new CodexListener(this), this);
        PerksCommand perksCommand = new PerksCommand(this);
        getCommand("perks").setExecutor(perksCommand);
        getCommand("perks").setTabCompleter(perksCommand);

        databaseManager = new DatabaseManager(this);
        databaseManager.initDatabase();
        databaseProvider = new DatabaseProvider(this);



        getLogger().info("[SMPplugin] Plugin enabled");
    }
    public void initConfig() {
        this.saveDefaultConfig();
        this.reloadConfig();
        configPerkManager.LoadConfigAtStart();
    }
    public Connection getConnexion() throws SQLException {
        return databaseManager.getConnection();
    }
    public void reloadAllConfig() {
        this.reloadConfig();
        configPerkManager.ReloadConfig();
    }
    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }
}
