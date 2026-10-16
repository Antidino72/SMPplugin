package fr.antidino.smp.core.manager;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.librairie.ItemBuilder;
import fr.antidino.smp.librairie.PDCBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class SlotManager {
    CorePlugin plugin;
    public  SlotManager(CorePlugin plugin) {
        this.plugin = plugin;
    }
    public ItemStack initializeSlots(ItemStack item){

        if(item == null) {
            return null;
        };
        Material material = item.getType();
        ItemStack itemStack = new ItemBuilder(material,plugin).build();
        itemStack = plugin.pdcBuilder.initializeSlots(itemStack);

        return itemStack;
    }
    public ItemStack applyPerkToNextAvailableSlot(ItemStack equipment, String perkId) {
        ItemMeta meta = equipment.getItemMeta();
        if (meta == null) return equipment;
        PDCBuilder builder = plugin.pdcBuilder;
        if (!(builder.hasEmptySlot(equipment))) {return equipment;}
        return builder.addPerkToItem(equipment, perkId);
    }
}
