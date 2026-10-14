package fr.antidino.smp.core.manager.listenner;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.manager.perk.AbstractPerk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemBreakListener implements Listener {
    private static final Logger log = LoggerFactory.getLogger(ItemBreakListener.class);
    CorePlugin plugin;
    public ItemBreakListener(CorePlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onItemBreak(PlayerItemBreakEvent event) {
        plugin.pdcBuilder.getAppliedPerks(event.getBrokenItem()).forEach(perk -> {
            AbstractPerk perk_object =plugin.perkManager.getPerk(perk);
            event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), perk_object.getItem());
        });
    }
}
