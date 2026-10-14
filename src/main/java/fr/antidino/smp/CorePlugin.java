package fr.antidino.smp;

import fr.antidino.smp.core.manager.SlotManager;
import fr.antidino.smp.core.manager.commands.AdminCommands;
import fr.antidino.smp.core.manager.listenner.CraftingListener;
import fr.antidino.smp.core.manager.listenner.ItemBreakListener;
import fr.antidino.smp.core.manager.listenner.SmithingListener;
import fr.antidino.smp.core.SmithingRegisterAllItem;
import fr.antidino.smp.core.manager.perk.PerkManager;
import fr.antidino.smp.librairie.PDCBuilder;
import org.bukkit.plugin.java.JavaPlugin;

public final class CorePlugin extends JavaPlugin {
    public PDCBuilder pdcBuilder;
    public PerkManager perkManager;
    public SlotManager slotManager;


    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new SmithingListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemBreakListener(this), this);

        pdcBuilder = new PDCBuilder(this);
        PDCBuilder.init(this);
        perkManager = new PerkManager(this);
        slotManager = new SlotManager(this);
        new SmithingRegisterAllItem(this);
        CraftingListener craftingListenner = new CraftingListener(this);
        getCommand("perk").setExecutor(new AdminCommands(this));

        initConfig();

        getLogger().info("[SMPplugin] Plugin enabled");
    }
    public void initConfig() {
        this.saveDefaultConfig();
        this.reloadConfig();
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
