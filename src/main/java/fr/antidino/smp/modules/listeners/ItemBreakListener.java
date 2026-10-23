package fr.antidino.smp.modules.listeners;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.modules.perk.AbstractPerk;
import fr.antidino.smp.modules.perk.PerkEntry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;

public class ItemBreakListener implements Listener {
    final CorePlugin plugin;
    public ItemBreakListener(CorePlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onItemBreak(PlayerItemBreakEvent event) {
        plugin.pdcBuilder.getAppliedPerks(event.getBrokenItem()).forEach(raw -> {
            PerkEntry entry = PerkEntry.parse(raw);
            AbstractPerk perk_object = plugin.perkManager.getPerk(entry.id());
            if (perk_object == null) return;
            // le perk est rendu avec son niveau
            event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), perk_object.getItem(entry.level()));
        });
    }
}
