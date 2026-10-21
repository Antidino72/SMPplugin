package fr.antidino.smp.core.listeners;

import fr.antidino.smp.CorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class VoidItemSaver extends BukkitRunnable {

    private final CorePlugin plugin;
    private static final double SAFE_Y = 64.0; // Y de téléportation

    public VoidItemSaver(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (World world : Bukkit.getWorlds()) {
            int minY = world.getMinHeight(); // -64 pour les mondes 1.18+

            for (Entity entity : world.getEntities()) {
                if (entity.getType() != EntityType.ITEM) continue;

                Item item = (Item) entity;
                if (!isAPerkItem(item.getItemStack())) return;
                if (item.getLocation().getY() < minY + 5) {

                    Location safeLoc = item.getLocation().clone();
                    safeLoc.setY(SAFE_Y);
                    item.teleport(safeLoc);


                    // item.remove();
                }
            }
        }
    }

    public void start() {
        // Toutes les 10 ticks (0.5 seconde)
        this.runTaskTimer(plugin, 0L, 10L);
    }
    private boolean isAPerkItem(ItemStack itemStack) {
        String perk_id = plugin.perkManager.getPerkId(itemStack);
        return perk_id != null;
    }
}