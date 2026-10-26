package fr.antidino.smp.modules.listeners;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.modules.perk.AbstractPerk;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class PlayerJoinQuitEvent implements Listener {
    CorePlugin plugin;
    public PlayerJoinQuitEvent(CorePlugin plugin) {
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
           plugin.playerPerkCache.update(event.getPlayer());
        });
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.playerPerkCache.cleanup(event.getPlayer());
    }
}
