package fr.antidino.smp.core.listeners;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.SmithingRegisterAllItem;
import fr.antidino.smp.core.manager.SlotManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class CraftingListener implements Listener {

    CorePlugin corePlugin;
    SlotManager slotManager;

    public CraftingListener(CorePlugin plugin) {
        this.corePlugin = plugin;
        this.slotManager = new SlotManager(plugin);
    }

    @EventHandler
    public void onCraftingEvent(CraftItemEvent event) {
        ItemStack resultType = event.getInventory().getResult();
        /*
        if (!(event.getWhoClicked() instanceof Player player)) return;


        if (resultType == null || !resultType.hasItemMeta()) return;

        String id = corePlugin.perkManager.getPerkId(resultType);
        if (id != null && !event.isCancelled()) {

            long currentTime = System.currentTimeMillis();
            UUID playerId = player.getUniqueId();

            if (cooldowns.containsKey(playerId)) {
                long lastCraftTime = cooldowns.get(playerId);
                if (currentTime - lastCraftTime < 1000) {
                    return;
                }
            }
            cooldowns.put(playerId, currentTime);
            corePlugin.databaseProvider.InsertPerkCraftSQL(id, playerId.toString());
        }

        */
        for (Material material : SmithingRegisterAllItem.getAllMaterials()) {
            assert resultType != null;
            if (material == resultType.getType()) {
                ItemStack item = event.getRecipe().getResult();
                item = slotManager.initializeSlots(item);
                event.getInventory().setResult(item);
                break;
            }
        }
    }
}