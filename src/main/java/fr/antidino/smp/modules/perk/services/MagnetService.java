package fr.antidino.smp.modules.perk.services;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.modules.perk.AbstractPerk;
import fr.antidino.smp.modules.perk.config.PerkTypeId;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.UUID;

public class MagnetService {
    private final CorePlugin plugin;
    public MagnetService(CorePlugin plugin) {
        this.plugin = plugin;

    }
    public void magnet(Player player, int level) {
        if (level <= 0) return;
        AbstractPerk perk = plugin.perkManager.getPerk(PerkTypeId.MAGNET);
        if (perk == null) return;
        double radius = perk.getValue("radius", level, 3 + 2 * (level - 1));
        Vector target = player.getLocation().add(0, 0.6, 0).toVector();
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (!(entity instanceof Item item)) continue;
            if (item.getPickupDelay() > 0) continue;

            UUID thrower = item.getThrower();
            if (thrower != null && thrower.equals(player.getUniqueId())) continue;

            Vector direction = target.clone().subtract(item.getLocation().toVector());
            if (direction.lengthSquared() < 0.64) continue;
            ItemStack itemStack = item.getItemStack();
            player.getInventory().addItem(itemStack);
            item.remove();
        }
    }

}
