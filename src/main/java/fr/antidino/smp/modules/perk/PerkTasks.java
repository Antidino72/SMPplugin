package fr.antidino.smp.modules.perk;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.modules.perk.config.PerkTypeId;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Tâche répétée (toutes les 4 ticks) pour les perks "actifs en continu" portés sur l'armure :
 * vision nocturne, aimant, double saut.
 */
public class PerkTasks {
    private final CorePlugin plugin;
    private final Set<UUID> nightVision = new HashSet<>();
    private final Set<UUID> flightManaged = new HashSet<>();
    private final Map<UUID, Integer> jumpsLeft = new HashMap<>();
    private BukkitTask task;
    private int tick = 0;

    public PerkTasks(CorePlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (task != null) return;
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::run, 20L, 4L);
    }

    /** Arrêt propre : retire les effets/vols accordés par les perks. */
    public void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        for (Player player : Bukkit.getOnlinePlayers()) cleanup(player);
    }

    public boolean isManaged(UUID id) {
        return flightManaged.contains(id);
    }

    private void run() {
        tick++;
        boolean refreshNightVision = (tick % 10 == 0);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isDead()) continue;

            var cacheWrapper = plugin.playerPerkCache.getPerksCache(player.getUniqueId());
            if (cacheWrapper == null) continue;

            Map<EquipmentSlot, List<String>> perkcache = cacheWrapper.get();
            if (perkcache == null || perkcache.isEmpty()) continue;


            boolean hasNightVision = false;
            boolean hasMagnet = false;
            boolean hasDoubleJump = false;


            for (List<String> entries : perkcache.values()) {
                if (entries == null) continue;

                if (!hasNightVision && entries.contains(PerkTypeId.NIGHT_VISION)) {
                    hasNightVision = true;
                }
                if (!hasMagnet && entries.contains(PerkTypeId.MAGNET)) {
                    hasMagnet = true;
                }
                if (!hasDoubleJump && entries.contains(PerkTypeId.DOUBLE_JUMP)) {
                    hasDoubleJump = true;
                }
            }


            nightVision(player, hasNightVision ? 1 : 0, refreshNightVision);
            magnet(player, hasMagnet ? 3 : 0);
            doubleJump(player, hasDoubleJump ? 1 : 0);
        }
    }

    // ------------------------------------------------------- VISION NOCTURNE

    private void nightVision(Player player, int level, boolean refresh) {
        UUID id = player.getUniqueId();
        if (level > 0) {
            if (refresh || !nightVision.contains(id)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 500, 0, true, false, false));
                nightVision.add(id);
            }
        } else if (nightVision.remove(id)) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
    }

    // ----------------------------------------------------------------- AIMANT

    private void magnet(Player player, int level) {
        if (level <= 0) return;
        AbstractPerk perk = plugin.perkManager.getPerk(PerkTypeId.MAGNET);
        if (perk == null) return;
        double radius = perk.getValue("radius", level, 3 + 2 * (level - 1));
        double speed = perk.getValue("speed", level, 0.35);

        Vector target = player.getLocation().add(0, 0.6, 0).toVector();
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (!(entity instanceof Item item)) continue;
            if (item.getPickupDelay() > 0) continue;

            UUID thrower = item.getThrower();
            if (thrower != null && thrower.equals(player.getUniqueId())) continue;

            Vector direction = target.clone().subtract(item.getLocation().toVector());
            if (direction.lengthSquared() < 0.64) continue;
            item.setVelocity(direction.normalize().multiply(speed));
        }
    }

    // ------------------------------------------------------------- DOUBLE SAUT

    private void doubleJump(Player player, int level) {
        UUID id = player.getUniqueId();
        GameMode mode = player.getGameMode();
        boolean survival = mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE;

        if (level <= 0 || !survival) {
            if (flightManaged.remove(id)) {
                jumpsLeft.remove(id);
                if (survival) {
                    player.setFlying(false);
                    player.setAllowFlight(false);
                }
            }
            return;
        }
        if (player.isGliding()) return;

        AbstractPerk perk = plugin.perkManager.getPerk(PerkTypeId.DOUBLE_JUMP);
        if (perk == null) return;

        flightManaged.add(id);
        if (player.isOnGround()) {
            jumpsLeft.put(id, (int) Math.max(1, perk.getValue("jumps", level, level)));
        }
        boolean canJump = jumpsLeft.getOrDefault(id, 0) > 0;
        if (player.getAllowFlight() != canJump) {
            player.setAllowFlight(canJump);
        }
    }

    /** Appelé par le listener quand un joueur "double-tape" saut en l'air. */
    public void performDoubleJump(Player player) {
        UUID id = player.getUniqueId();
        int left = jumpsLeft.getOrDefault(id, 0);
        if (left <= 0) return;
        jumpsLeft.put(id, left - 1);
        if (left - 1 <= 0) player.setAllowFlight(false);

        int level = plugin.perkManager.getArmorPerkLevels(player).getOrDefault(PerkTypeId.DOUBLE_JUMP, 1);
        AbstractPerk perk = plugin.perkManager.getPerk(PerkTypeId.DOUBLE_JUMP);
        double power = perk == null ? 0.9 : perk.getValue("power", level, 0.9);

        Vector velocity = player.getLocation().getDirection().setY(0);
        if (velocity.lengthSquared() > 0) velocity.normalize().multiply(0.35);
        velocity.setY(power);
        player.setVelocity(velocity);
        player.setFallDistance(0f);

        Location loc = player.getLocation();
        player.getWorld().spawnParticle(Particle.CLOUD, loc, 10, 0.25, 0.05, 0.25, 0.02);
        player.getWorld().playSound(loc, Sound.ENTITY_BAT_TAKEOFF, 0.6f, 1.4f);
    }

    /** Nettoyage à la déconnexion / à l'arrêt (évite de laisser le vol activé). */
    public void cleanup(Player player) {
        UUID id = player.getUniqueId();
        if (flightManaged.remove(id)) {
            jumpsLeft.remove(id);
            GameMode mode = player.getGameMode();
            if (mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }
        }
        if (nightVision.remove(id)) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
    }
}
