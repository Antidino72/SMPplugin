package fr.antidino.smp.modules.perk.listeners;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.CooldownManager;
import fr.antidino.smp.modules.perk.AbstractPerk;
import fr.antidino.smp.modules.perk.config.PerkTypeId;
import fr.antidino.smp.modules.perk.type.WardenPerk;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/** Effets événementiels : double saut, vampire (soin au coup), fonte auto des minerais. */
public class PerkEffectListener implements Listener {
    private final CorePlugin plugin;

    private record Smelt(ItemStack result, float xp) {}
    private final CooldownManager cooldownManager = new CooldownManager();
    private final Map<Material, Optional<Smelt>> smeltCache = new HashMap<>();

    public PerkEffectListener(CorePlugin plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------- DOUBLE SAUT

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (!plugin.perkTasks.servicesManager.doubleJumpService.isManaged(player.getUniqueId())) return;
        if (!event.isFlying()) return;

        // Le vol n'est jamais réellement activé : on transforme le double-tap en saut
        event.setCancelled(true);
        player.setFlying(false);
        if (player.isOnGround()) return;
        plugin.perkTasks.servicesManager.doubleJumpService.performDoubleJump(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.perkTasks.cleanup(event.getPlayer());
    }

    // ----------------------------------------------------------------- VAMPIRE

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target) || target instanceof ArmorStand) return;

        if (!plugin.perkManager.playerPerkCache.getPerksCache(player.getUniqueId()).hasPerk(EquipmentSlot.HAND, PerkTypeId.LIFESTEAL_PERK)) return;
        AbstractPerk perk = plugin.perkManager.getPerk(PerkTypeId.LIFESTEAL_PERK);
        if (perk == null) return;

        double percent = perk.getValue("heal_percent", 1, 0.10 * 1);
        double heal = event.getFinalDamage() * percent;
        if (heal <= 0) return;

        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        double max = maxHealth == null ? 20.0 : maxHealth.getValue();
        player.setHealth(Math.min(max, player.getHealth() + heal));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.4F, 1);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1.8, 0), 4, 0.2, 0.1, 0.2, 0);
    }

    // ----------------------------------------------------------------- FONTE AUTO

    @EventHandler(ignoreCancelled = true)
    public void onBlockDrop(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        if (plugin.perkManager.playerPerkCache.getPerksCache(player.getUniqueId()).hasPerk(EquipmentSlot.HAND, PerkTypeId.SMELTER)) return;
        if (!isOre(event.getBlockState().getType())) return;

        boolean smelted = false;
        for (Item drop : event.getItems()) {
            ItemStack stack = drop.getItemStack();
            // Si le minerai tombe tel quel (Toucher de soie), on ne le fond pas
            if (isOre(stack.getType())) continue;

            Optional<Smelt> smelt = smeltCache.computeIfAbsent(stack.getType(), _ -> Optional.ofNullable(findSmelt(stack)));
            if (smelt.isEmpty()) continue;

            ItemStack result = smelt.get().result().clone();
            result.setAmount(Math.min(result.getMaxStackSize(), stack.getAmount() * result.getAmount()));
            drop.setItemStack(result);

            int xp = Math.round(smelt.get().xp() * stack.getAmount());
            if (xp > 0) player.giveExp(xp);
            smelted = true;
        }
        if (smelted) {
            event.getBlock().getWorld().spawnParticle(Particle.FLAME,
                    event.getBlock().getLocation().add(0.5, 0.5, 0.5), 6, 0.2, 0.2, 0.2, 0.01);
        }
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
                String message = plugin.perkManager.configPerkManager.WardenConfig.getString("cooldown-message",
                                "Tu dois attendre encore %time%s avant de réutiliser cette capacité !")
                        .replace("%time%", String.valueOf(remaining));
                player.sendMessage(MiniMessage.miniMessage().deserialize(message));
                return;
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            if(player.isSneaking() && item.getType() == Material.AIR && item.isEmpty()) {
                WardenPerk.triggerSonicBoom(player);
                cooldownManager.setCooldown(player.getUniqueId(), plugin.perkManager.configPerkManager.WardenConfig.getInt("cooldown",5));
                event.setCancelled(true);
            }
        }
    }
    private boolean isOre(Material material) {
        return material.name().endsWith("_ORE") || material == Material.ANCIENT_DEBRIS;
    }

    private Smelt findSmelt(ItemStack input) {
        Iterator<Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            if (it.next() instanceof FurnaceRecipe furnace && furnace.getInputChoice().test(input)) {
                return new Smelt(furnace.getResult().clone(), furnace.getExperience());
            }
        }
        return null;
    }
}
