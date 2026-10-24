package fr.antidino.smp;

import fr.antidino.smp.modules.listeners.*;
import fr.antidino.smp.core.MessageManager;
import fr.antidino.smp.modules.perk.slotExpander.SlotExpander;
import fr.antidino.smp.modules.perk.slotExpander.SlotManager;
import fr.antidino.smp.modules.perk.codex.CodexListener;
import fr.antidino.smp.modules.perk.codex.CodexMenu;
import fr.antidino.smp.modules.commands.AdminCommands;
import fr.antidino.smp.modules.commands.PerksCommand;
import fr.antidino.smp.core.database.DatabaseManager;
import fr.antidino.smp.core.database.DatabaseProvider;
import fr.antidino.smp.modules.perk.SmithingRegisterAllItem;
import fr.antidino.smp.modules.perk.config.ConfigPerkManager;
import fr.antidino.smp.modules.perk.AbstractPerk;
import fr.antidino.smp.modules.perk.listeners.PerkEffectListener;
import fr.antidino.smp.modules.perk.PerkManager;
import fr.antidino.smp.modules.perk.PerkTasks;
import fr.antidino.smp.modules.perk.rarity.RarityManager;
import fr.antidino.smp.core.Utils.PDCBuilder;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class CorePlugin extends JavaPlugin {
    public PDCBuilder pdcBuilder;
    public PerkManager perkManager;
    public SlotManager slotManager;
    public ConfigPerkManager configPerkManager;
    public MessageManager messageManager;
    public CodexMenu codexMenu;
    public PerkTasks perkTasks;
    public SlotExpander slotExpander;
    public RarityManager rarityManager;

    private DatabaseManager databaseManager;
    public DatabaseProvider databaseProvider;
    @Override
    public void onEnable() {
        configPerkManager = new ConfigPerkManager(this);
        initConfig();
        messageManager = new MessageManager(this);

        getServer().getPluginManager().registerEvents(new CraftingListener(this), this);
        getServer().getPluginManager().registerEvents(new SmithingListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new SavingPerkListeners(this), this);

        rarityManager = new RarityManager(this);
        pdcBuilder = new PDCBuilder(this);

        PDCBuilder.init(this);
        perkManager = new PerkManager(this);
        slotManager = new SlotManager(this);
        SmithingRegisterAllItem.RegisterAllItem(this);

        slotExpander = new SlotExpander(this);
        perkTasks = new PerkTasks(this);
        perkTasks.start();
        getServer().getPluginManager().registerEvents(new PerkEffectListener(this), this);

        codexMenu = new CodexMenu(this);
        getServer().getPluginManager().registerEvents(new CodexListener(this), this);
        PerksCommand perksCommand = new PerksCommand(this);
        AdminCommands adminCommands = new AdminCommands(this);

        getCommand("perk").setExecutor(adminCommands);
        getCommand("perk").setTabCompleter(adminCommands);
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
        if (rarityManager != null) rarityManager.reload();
        if (slotExpander != null) slotExpander.reload();

        List<NamespacedKey> keys = perkManager.getPerks().stream().map(AbstractPerk::getKey).toList();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.discoverRecipes(keys);
        }
        return perkManager.getPerks().size();
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