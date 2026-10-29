package fr.antidino.smp.modules.listeners;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import fr.antidino.smp.CorePlugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;

public class InventaireChangeListeners implements Listener {
    private final CorePlugin plugin;

    public InventaireChangeListeners(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerArmorChange(PlayerArmorChangeEvent event) {
        Player player = event.getPlayer();
        EquipmentSlot slot = event.getSlot();
        plugin.perkManager.playerPerkCache.updateSlot(player, slot);
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSlotChange(PlayerItemHeldEvent event) {

        plugin.perkManager.playerPerkCache.updatePlayerHandCache(event.getPlayer());
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        plugin.perkManager.playerPerkCache.updatePlayerHandCache(event.getPlayer());
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;


        int rawSlot = event.getRawSlot();
        if (isHandSlot(rawSlot)) {
            plugin.perkManager.playerPerkCache.updatePlayerHandCache(player);
        }
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            plugin.perkManager.playerPerkCache.updatePlayerHandCache(player);
        }
    }

    private boolean isHandSlot(int rawSlot) {

        return rawSlot >= 36 && rawSlot <= 45;
    }
}
