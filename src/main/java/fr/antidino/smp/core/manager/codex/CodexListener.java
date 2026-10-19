package fr.antidino.smp.core.manager.codex;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.manager.perk.AbstractPerk;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.List;

public class CodexListener implements Listener {
    private final CorePlugin plugin;

    public CodexListener(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof CodexMenu.CodexHolder holder)) return;

        // Rien ne doit pouvoir être pris, posé ou déplacé (shift-clic, double-clic, touches...)
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getInventory()) return;

        int slot = event.getSlot();

        if (holder.type == CodexMenu.Type.LIST) {
            handleList(player, holder, slot);
        } else {
            if (slot == CodexMenu.BACK_SLOT) {
                click(player);
                plugin.codexMenu.openList(player, holder.page);
            }
        }
    }

    private void handleList(Player player, CodexMenu.CodexHolder holder, int slot) {
        if (slot == CodexMenu.CLOSE_SLOT) {
            player.closeInventory();
            return;
        }
        if (slot == CodexMenu.PREV_SLOT && holder.page > 0) {
            click(player);
            plugin.codexMenu.openList(player, holder.page - 1);
            return;
        }
        if (slot == CodexMenu.EXPANDER_SLOT && plugin.slotExpander != null) {
            click(player);
            plugin.codexMenu.openExpanderDetail(player, holder.page);
            return;
        }
        if (slot == CodexMenu.NEXT_SLOT) {
            List<AbstractPerk> perks = plugin.codexMenu.getSortedPerks();
            if ((holder.page + 1) * CodexMenu.PERKS_PER_PAGE < perks.size()) {
                click(player);
                plugin.codexMenu.openList(player, holder.page + 1);
            }
            return;
        }
        if (slot >= 0 && slot < CodexMenu.PERKS_PER_PAGE) {
            List<AbstractPerk> perks = plugin.codexMenu.getSortedPerks();
            int index = holder.page * CodexMenu.PERKS_PER_PAGE + slot;
            if (index < perks.size()) {
                click(player);
                plugin.codexMenu.openDetail(player, perks.get(index), holder.page);
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof CodexMenu.CodexHolder) {
            event.setCancelled(true);
        }
    }

    private void click(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.0f);
    }
}
