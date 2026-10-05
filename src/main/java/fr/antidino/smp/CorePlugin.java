package fr.antidino.smp;

import org.bukkit.plugin.java.JavaPlugin;

public final class CorePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
       getLogger().info("[SMPplugin] Plugin enabled");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
