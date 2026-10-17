package fr.antidino.smp.core.manager.listenner;

import fr.antidino.smp.CorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerJoinListener implements Listener {
    private static final Logger log = LoggerFactory.getLogger(PlayerJoinListener.class);
    CorePlugin plugin;
    public PlayerJoinListener(CorePlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.perkManager.getPerks().forEach(perk -> {
                log.info("Register recipe ... "+ perk.getKey());
                event.getPlayer().discoverRecipe(perk.getKey());
            });

        });
    }
}
