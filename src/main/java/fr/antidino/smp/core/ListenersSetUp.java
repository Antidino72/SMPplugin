package fr.antidino.smp.core;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.modules.listeners.*;
import fr.antidino.smp.modules.perk.codex.CodexListener;
import fr.antidino.smp.modules.perk.listeners.PerkEffectListener;

public class ListenersSetUp {
    public static void registerListeners(CorePlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new CraftingListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new SmithingListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ItemBreakListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinQuitEvent(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new SavingPerkListeners(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new InventaireChangeListeners(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new CodexListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PerkEffectListener(plugin), plugin);


    }
}
