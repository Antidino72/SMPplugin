package fr.antidino.smp;

import fr.antidino.smp.core.manager.SlotManager;
import fr.antidino.smp.core.manager.commands.AdminCommands;
import fr.antidino.smp.core.manager.listenner.CraftingListener;
import fr.antidino.smp.core.manager.listenner.ItemBreakListener;
import fr.antidino.smp.core.manager.listenner.SmithingListener;
import fr.antidino.smp.core.SmithingRegisterAllItem;
import fr.antidino.smp.core.manager.perk.config.ConfigPerkManager;
import fr.antidino.smp.core.manager.perk.PerkManager;
import fr.antidino.smp.librairie.PDCBuilder;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CorePlugin extends JavaPlugin {
    private static final Logger log = LoggerFactory.getLogger(CorePlugin.class);
    public PDCBuilder pdcBuilder;
    public PerkManager perkManager;
    public SlotManager slotManager;
    public ConfigPerkManager configPerkManager;


    @Override
    public void onEnable() {
        configPerkManager = new ConfigPerkManager(this);
        initConfig();


        getServer().getPluginManager().registerEvents(new SmithingListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemBreakListener(this), this);



        PDCBuilder.init(this);
        perkManager = new PerkManager(this);
        slotManager = new SlotManager(this);
        new SmithingRegisterAllItem(this);
        CraftingListener craftingListenner = new CraftingListener(this);
        getCommand("perk").setExecutor(new AdminCommands(this));
        NamespacedKey key = new NamespacedKey(this, "perk_health");
        Recipe recipe = Bukkit.getRecipe(key);
        if (recipe != null) {
            this.getLogger().info("La recette " + "perk_health" + " est bien enregistrée !");
        } else {
            this.getLogger().warning("La recette " + "perk_health" + " N'EST PAS enregistrée !");
        }



        getLogger().info("[SMPplugin] Plugin enabled");
    }
    public void initConfig() {
        this.saveDefaultConfig();
        this.reloadConfig();
        configPerkManager.LoadConfigAtStart();


    }
    public void reloadAllConfig() {
        this.reloadConfig();
        configPerkManager.ReloadConfig();
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
