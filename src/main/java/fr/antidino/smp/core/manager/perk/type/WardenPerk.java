package fr.antidino.smp.core.manager.perk.type;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.manager.perk.AbstractPerk;
import fr.antidino.smp.librairie.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

public class WardenPerk extends AbstractPerk {
    public WardenPerk(String id, CorePlugin plugin, FileConfiguration config) {
        super(id, plugin, config);
    }

    @Override
    public ItemStack onApply(ItemStack item) {
        return new ItemBuilder(item,this.plugin).build();
    }

    @Override
    public void onRemove(Player player, ItemStack item) {

    }
    public static void triggerSonicBoom(Player caster) {
        Location startLoc = caster.getEyeLocation();
        Vector direction = startLoc.getDirection().normalize();


        caster.getWorld().playSound(startLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 3.0f, 1.0f);


        for (double d = 1; d <= 15; d += 0.5) {
            Location currentLoc = startLoc.clone().add(direction.clone().multiply(d));


            caster.getWorld().spawnParticle(Particle.SONIC_BOOM, currentLoc, 1, 0, 0, 0, 0);


            for (org.bukkit.entity.Entity entity : currentLoc.getWorld().getNearbyEntities(currentLoc, 0.8, 0.8, 0.8)) {
                if (entity instanceof LivingEntity target && !entity.equals(caster)) {
                    target.damage(15.0, caster);
                    target.setVelocity(direction.clone().multiply(1.5).setY(0.4));
                    return;
                }
            }
        }
    }
}
