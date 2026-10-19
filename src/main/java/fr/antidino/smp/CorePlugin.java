package fr.antidino.smp;

import fr.antidino.smp.core.manager.SlotExpander;
import fr.antidino.smp.core.manager.SlotManager;
import fr.antidino.smp.core.manager.codex.CodexListener;
import fr.antidino.smp.core.manager.codex.CodexMenu;
import fr.antidino.smp.core.manager.commands.AdminCommands;
import fr.antidino.smp.core.manager.commands.PerksCommand;
import fr.antidino.smp.core.manager.database.DatabaseManager;
import fr.antidino.smp.core.manager.database.DatabaseProvider;
import fr.antidino.smp.core.manager.listenner.CraftingListener;
import fr.antidino.smp.core.manager.listenner.ItemBreakListener;
import fr.antidino.smp.core.manager.listenner.PlayerJoinListener;
import fr.antidino.smp.core.manager.listenner.SmithingListener;
import fr.antidino.smp.core.SmithingRegisterAllItem;
import fr.antidino.smp.core.manager.perk.config.ConfigPerkManager;
import fr.antidino.smp.core.manager.perk.AbstractPerk;
import fr.antidino.smp.core.manager.perk.PerkEffectListener;
import fr.antidino.smp.core.manager.perk.PerkManager;
import fr.antidino.smp.core.manager.perk.PerkTasks;
import fr.antidino.smp.librairie.PDCBuilder;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class CorePlugin extends JavaPlugin {
    private static final Logger log = LoggerFactory.getLogger(CorePlugin.class);
    public PDCBuilder pdcBuilder;
    public PerkManager perkManager;
    public SlotManager slotManager;
    public ConfigPerkManager configPerkManager;
    public CodexMenu codexMenu;
    public SlotExpander slotExpander;
    public PerkTasks perkTasks;

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
        new CraftingListener(this);

        pdcBuilder = new PDCBuilder(this);

        PDCBuilder.init(this);
        perkManager = new PerkManager(this);
        slotManager = new SlotManager(this);
        new SmithingRegisterAllItem(this);
        getCommand("perk").setExecutor(new AdminCommands(this));

        slotExpander = new SlotExpander(this);
        perkTasks = new PerkTasks(this);
        perkTasks.start();
        getServer().getPluginManager().registerEvents(new PerkEffectListener(this), this);

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
    /** Recharge config.yml, tous les yml de perks, les perks (crafts + niveaux) et l'item de slots. */
    public int fullReload() {
        reloadConfig();
        configPerkManager.LoadConfigAtStart();
        perkManager.reload();
        if (slotExpander != null) slotExpander.reload();

        List<NamespacedKey> keys = perkManager.getPerks().stream().map(AbstractPerk::getKey).toList();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.discoverRecipes(keys);
        }
        return perkManager.getPerks().size();
    }
    public void reloadAllConfig() {
        this.reloadConfig();
        configPerkManager.ReloadConfig();
    }
    @Override
    public void onDisable() {
        if (perkTasks != null) {
            perkTasks.shutdown();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
    }
}
