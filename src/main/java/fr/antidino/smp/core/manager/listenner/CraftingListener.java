package fr.antidino.smp.core.manager.listenner;

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
        this.corePlugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.slotManager = new SlotManager(plugin);
    }

    @EventHandler
    public void OnCraftingEvent(CraftItemEvent event){

        if (event.getRecipe().getResult() == null){
            return;
        }
        Material resultType = event.getRecipe().getResult().getType();
        for (Material material : SmithingRegisterAllItem.getAllMaterials()) {
            if (material == resultType) {
                ItemStack item = event.getRecipe().getResult();
                item = slotManager.initializeSlots(item);
                event.getInventory().setResult(item);
                event.getInventory().addItem(item);
                break;
            }
        }
    }
}
