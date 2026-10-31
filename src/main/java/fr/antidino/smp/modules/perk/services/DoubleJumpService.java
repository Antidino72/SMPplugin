package fr.antidino.smp.modules.perk.services;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.modules.perk.AbstractPerk;
import fr.antidino.smp.modules.perk.config.PerkTypeId;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class DoubleJumpService {
    private final CorePlugin plugin;
    private final Set<UUID> flightManaged = new HashSet<>();

    private final Map<UUID, Integer> jumpsLeft = new HashMap<>();
    public DoubleJumpService(CorePlugin plugin) {
        this.plugin = plugin;
    }
    public boolean isManaged(UUID id) {
        return flightManaged.contains(id);
    }

    public void doubleJump(Player player, int level) {
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

        AbstractPerk perk = plugin.perkManager.getPerk(PerkTypeId.DOUBLE_JUMP);
        double power = perk == null ? 0.9 : perk.getValue("power", 1, 0.9);

        Vector velocity = player.getLocation().getDirection().setY(0);
        if (velocity.lengthSquared() > 0) velocity.normalize().multiply(0.35);
        velocity.setY(power);
        player.setVelocity(velocity);
        player.setFallDistance(0f);

        Location loc = player.getLocation();
        player.getWorld().spawnParticle(Particle.CLOUD, loc, 10, 0.25, 0.05, 0.25, 0.02);
        player.getWorld().playSound(loc, Sound.ENTITY_BAT_TAKEOFF, 0.6f, 1.4f);
    }
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
    }



}
