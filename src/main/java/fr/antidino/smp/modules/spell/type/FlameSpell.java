package fr.antidino.smp.modules.spell.type;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.modules.spell.AbstractSpell;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class FlameSpell extends AbstractSpell {
    public FlameSpell(String id , String spellName, FileConfiguration spellConfig, CorePlugin plugin) {
        super(id,spellName, spellConfig,plugin);
    }

    @Override
    public void cast(Player player) {
        Location startLoc = player.getEyeLocation();
        Vector direction = startLoc.getDirection().normalize();

        player.setFireTicks(0);
        player.getWorld().playSound(startLoc, Sound.ITEM_FIRECHARGE_USE, 3.0f, 1.0f);
        new BukkitRunnable() {
            double d = 1;
            final double maxDistance = 15;
            final double step = 0.5;

            @Override
            public void run() {
                // Si le projectile va trop loin, on arrête la tâche
                if (d > maxDistance) {
                    this.cancel();
                    return;
                }

                Location currentLoc = startLoc.clone().add(direction.clone().multiply(d));

                // Affichage des particules
                player.getWorld().spawnParticle(Particle.ENCHANTED_HIT, currentLoc, 1, 0, 1, 0, 0);
                player.getWorld().spawnParticle(Particle.FLAME, currentLoc, 10, 0, 0, 0, 10);

                // Vérification des entités touchées
                for (Entity entity : currentLoc.getWorld().getNearbyEntities(currentLoc, 0.8, 0.8, 0.8)) {
                    if (entity instanceof LivingEntity target && !entity.equals(player)) {
                        target.setFireTicks(20 * 10);
                        target.damage(10.0, player);
                        target.setVelocity(direction.clone().multiply(1.6).setY(0.6));

                        // On arrête le sort dès qu'il touche une entité
                        this.cancel();
                        return;
                    }
                }

                // On avance d'un pas pour le prochain tick
                d += step;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
