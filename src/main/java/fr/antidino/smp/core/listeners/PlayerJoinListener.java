package fr.antidino.smp.core.listeners;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.manager.perk.AbstractPerk;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PlayerJoinListener implements Listener {
    private static final Logger log = LoggerFactory.getLogger(PlayerJoinListener.class);
    CorePlugin plugin;
    public PlayerJoinListener(CorePlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        List<NamespacedKey> keys = plugin.perkManager.getPerks().stream()
                .map(AbstractPerk::getKey)
                .toList();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            event.getPlayer().discoverRecipes(keys);
        }, 20L);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
           if ( !plugin.databaseProvider.hasEntry(event.getPlayer().getUniqueId().toString())){
               plugin.databaseProvider.insertPlayerSQL(event.getPlayer());
           }
        });
    }
}
