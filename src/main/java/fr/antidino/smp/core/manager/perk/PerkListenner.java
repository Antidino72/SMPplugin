package fr.antidino.smp.core.manager.perk;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.manager.CooldownManager;
import fr.antidino.smp.core.manager.perk.type.PerkTypeId;
import fr.antidino.smp.core.manager.perk.type.WardenPerk;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PerkListenner implements Listener {
    private static final Logger log = LoggerFactory.getLogger(PerkListenner.class);
    private final CorePlugin plugin;
    private final CooldownManager cooldownManager = new CooldownManager();
    public PerkListenner(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (!(event.getEntity() instanceof Player player)) return;
            if (plugin.perkManager.hasPerk(PerkTypeId.NO_FALL_PERK,player, EquipmentSlot.FEET)) event.setCancelled(true);
        }
    }
    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (plugin.perkManager.hasPerk(PerkTypeId.WARDEN_PERK,player, EquipmentSlot.CHEST)) {
            if (cooldownManager.hasCooldown(player.getUniqueId())) {
                int remaining = cooldownManager.getRemainingSeconds(player.getUniqueId());
                String message = plugin.configPerkManager.WardenConfig.getString("cooldown-message",
                        "Tu dois attendre encore %time%s avant de réutiliser cette capacité !")
                        .replace("%time%", String.valueOf(remaining));
                player.sendMessage(MiniMessage.miniMessage().deserialize(message));
                return;
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            if(player.isSneaking() && item.getType() == Material.AIR && item.isEmpty()) {
                WardenPerk.triggerSonicBoom(player);
                cooldownManager.setCooldown(player.getUniqueId(), plugin.configPerkManager.WardenConfig.getInt("cooldown",5));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        log.info(event.getCause().toString());
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
        if (event.getDamager() instanceof Player player) {
            if (plugin.perkManager.hasPerk(PerkTypeId.LIFESTEAL_PERK,player, EquipmentSlot.HAND)){
                double amount = event.getDamage();
                double lifesteal = amount * plugin.configPerkManager.LifeStealConfig.getDouble("amount", 5) / 100;
                double newHealth = Math.min(player.getHealth() + lifesteal, player.getAttribute(Attribute.MAX_HEALTH).getValue());
                player.setHealth(newHealth);
            }else {
                log.info("Not have lifesteal boost.");
            }
        }else {
            log.info(event.getDamager().toString());
        }
    }
}



