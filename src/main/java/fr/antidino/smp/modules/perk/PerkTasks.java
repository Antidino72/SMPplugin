package fr.antidino.smp.modules.perk;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.modules.perk.config.PerkTypeId;
import fr.antidino.smp.modules.perk.services.ServicesManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitTask;
import java.util.*;

/**
 * Tâche répétée (toutes les 4 ticks) pour les perks "actifs en continu" portés sur l'armure :
 * vision nocturne, aimant, double saut.
 */
public class PerkTasks {
    private final CorePlugin plugin;
    public final ServicesManager servicesManager ;
    private BukkitTask task;
    private int tick = 0;

    public PerkTasks(CorePlugin plugin) {

        this.plugin = plugin;
        this.servicesManager = new ServicesManager(plugin);
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


    private void run() {
        tick++;
        boolean refreshNightVision = (tick % 10 == 0);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isDead()) continue;

            var cacheWrapper = plugin.perkManager.playerPerkCache.getPerksCache(player.getUniqueId());
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

            servicesManager.nightVisionService.nightVision(player,hasNightVision? 1 : 0 , refreshNightVision);

            servicesManager.magnetService.magnet(player, hasMagnet ? 3 : 0);
            servicesManager.doubleJumpService.doubleJump(player, hasDoubleJump ? 1 : 0);
        }
    }


    /** Nettoyage à la déconnexion / à l'arrêt (évite de laisser le vol activé). */
    public void cleanup(Player player) {
        servicesManager.nightVisionService.cleanup(player);
        servicesManager.doubleJumpService.cleanup(player);
    }
}
