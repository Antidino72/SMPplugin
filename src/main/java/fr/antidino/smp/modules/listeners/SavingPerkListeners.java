package fr.antidino.smp.modules.listeners;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import fr.antidino.smp.CorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SavingPerkListeners implements Listener {
    private static final Logger log = LoggerFactory.getLogger(SavingPerkListeners.class);
    CorePlugin plugin;
    private static final double SAFE_Y = 64.0;

    public SavingPerkListeners(CorePlugin plugin) {
        this.plugin = plugin;


    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Item item))return;
        if (isAPerkItem(item.getItemStack())){
            event.setCancelled(true);
            item.setFireTicks(0);
        }

    }
    @EventHandler
    public void onEntityRemove(EntityRemoveFromWorldEvent event) {
        if (!(event.getEntity() instanceof Item item)) return;
        if (item.getLocation().getY() > 0) return;
        if (!isAPerkItem(item.getItemStack()))return;
        Location loc = item.getLocation();
        World world = loc.getWorld();
        Location chestLoc = new Location(world, loc.getBlockX(), SAFE_Y, loc.getBlockZ());
        Bukkit.getScheduler().runTask(plugin, () -> placeChestWithItem(chestLoc, item.getItemStack()));
    }
    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        Item victim = event.getEntity();
        World world = victim.getWorld();
        ItemStack itemStack = victim.getItemStack();
        if (victim.getLocation().getY() < world.getMinHeight()) {
            event.setCancelled(true);
        }
        event.setCancelled(isAPerkItem(itemStack));
    }

    private boolean isAPerkItem(ItemStack itemStack) {
        String perk_id = plugin.perkManager.getPerkId(itemStack);
        return perk_id != null;
    }
    private static void placeChestWithItem(Location location, ItemStack itemStack) {
        Block block = location.getBlock();
        block.setType(Material.CHEST);

        if (block.getState() instanceof Chest chest) {
            chest.getInventory().addItem(itemStack);
        }
    }
}