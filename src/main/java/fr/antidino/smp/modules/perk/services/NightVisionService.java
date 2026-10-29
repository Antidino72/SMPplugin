package fr.antidino.smp.modules.perk.services;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NightVisionService {
    private final Set<UUID> nightVision = new HashSet<>();
    public void nightVision(Player player, int level, boolean refresh) {
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
    public void cleanup(Player player) {
        UUID id = player.getUniqueId();
        if (nightVision.remove(id)) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
    }
}
