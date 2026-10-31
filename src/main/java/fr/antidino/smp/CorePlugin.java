package fr.antidino.smp;

import fr.antidino.smp.core.CommandSetUp;
import fr.antidino.smp.core.ListenersSetUp;
import fr.antidino.smp.modules.listeners.*;
import fr.antidino.smp.core.MessageManager;
import fr.antidino.smp.modules.perk.*;
import fr.antidino.smp.modules.perk.cache.DataCache;
import fr.antidino.smp.modules.perk.papi.PapiExpansion;
import fr.antidino.smp.modules.perk.slotExpander.SlotExpander;
import fr.antidino.smp.modules.perk.slotExpander.SlotManager;
import fr.antidino.smp.modules.perk.codex.CodexMenu;
import fr.antidino.smp.core.database.DatabaseProvider;
import fr.antidino.smp.modules.perk.rarity.RarityManager;
import fr.antidino.smp.core.Utils.PDCBuilder;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;

public final class CorePlugin extends JavaPlugin {
    private static final Logger log = LoggerFactory.getLogger(CorePlugin.class);


    // Librairie
    public PDCBuilder pdcBuilder;
    public MessageManager messageManager;
    public DatabaseProvider databaseProvider;


    public PerkManager perkManager;
    public SlotManager slotManager;
    public DataCache dataCache;
    public CodexMenu codexMenu;
    public PerkTasks perkTasks;
    public RarityManager rarityManager;

    @Override
    public void onEnable() {
           try {
                new PapiExpansion(this).register();
           }catch (Exception e){
               log.info("PAPI Expansion Error");
           }


        pdcBuilder = new PDCBuilder(this);
        PDCBuilder.init(this);

        perkManager = new PerkManager(this);
        dataCache = new DataCache(this);
        dataCache.load();
        initConfig();
        messageManager = new MessageManager(this);
        rarityManager = new RarityManager(this);

        slotManager = new SlotManager(this);
        SmithingRegisterAllItem.RegisterAllItem(this);


        perkTasks = new PerkTasks(this);
        perkTasks.start();


        codexMenu = new CodexMenu(this);

        CommandSetUp.registerCommands(this);
        ListenersSetUp.registerListeners(this);

        databaseProvider = new DatabaseProvider(this);



        getLogger().info("[SMPplugin] Plugin enabled");
    }
    public void initConfig() {
        this.saveDefaultConfig();
        this.reloadConfig();
        perkManager.configPerkManager.LoadConfigAtStart();
    }

    /** Recharge config.yml, tous les yml de perks, les perks (crafts + niveaux) et l'item de slots. */
    public int fullReload() {
        reloadConfig();
        perkManager.configPerkManager.LoadConfigAtStart();
        perkManager.perkRegistry.reload();
        perkManager.reload();
        if (rarityManager != null) rarityManager.reload();


        List<NamespacedKey> keys = perkManager.getPerks().stream().map(AbstractPerk::getKey).toList();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.discoverRecipes(keys);
        }
        dataCache.unload();
        dataCache.load();
        return perkManager.getPerks().size();

    }

    @Override
    public void onDisable() {
        if (perkTasks != null) {
            perkTasks.shutdown();
        }
        databaseProvider.CloseConnection();
    }
}