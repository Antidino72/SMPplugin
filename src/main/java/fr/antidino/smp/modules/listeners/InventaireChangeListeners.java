package fr.antidino.smp.modules.listeners;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import fr.antidino.smp.CorePlugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerChangeArmorListener implements Listener {
    private final CorePlugin plugin;

    public PlayerChangeArmorListener(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerArmorChange(PlayerArmorChangeEvent event) {
        Player player = event.getPlayer();
        EquipmentSlot slot = event.getSlot();
        plugin.playerPerkCache.updateSlot(player, slot);
        Bukkit.getLogger().info(player.getName() + " has changed armor!");
    }
}
