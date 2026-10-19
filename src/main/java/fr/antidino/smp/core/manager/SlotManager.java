package fr.antidino.smp.core.manager;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.librairie.ItemBuilder;
import fr.antidino.smp.librairie.PDCBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import fr.antidino.smp.core.manager.perk.PerkEntry;

import java.util.ArrayList;


public class SlotManager {
    CorePlugin plugin;
    public  SlotManager(CorePlugin plugin) {
        this.plugin = plugin;
    }
    public ItemStack initializeSlots(ItemStack item){

        if(item == null) {
            return null;
        }
        Material material = item.getType();
        ItemStack itemStack = new ItemBuilder(material,plugin).build();
        itemStack = plugin.pdcBuilder.initializeSlots(itemStack);

        return itemStack;
    }
    public ItemStack applyPerkToNextAvailableSlot(ItemStack equipment, String perkId) {
        return applyPerkToNextAvailableSlot(equipment, perkId, 1);
    }
    public ItemStack applyPerkToNextAvailableSlot(ItemStack equipment, String perkId, int level) {
        ItemMeta meta = equipment.getItemMeta();
        if (meta == null) return equipment;
        PDCBuilder builder = plugin.pdcBuilder;
        if (!(builder.hasEmptySlot(equipment))) {return equipment;}
        return builder.addPerkToItem(equipment, new PerkEntry(perkId, level).serialize());
    }

    /**
     * Ajoute des emplacements de perks à un équipement (plafonné à "cap").
     * Retourne le nouvel item, ou null si impossible (déjà au plafond).
     */
    public ItemStack expandSlots(ItemStack equipment, int add, int cap) {
        if (equipment == null || equipment.getType().isAir()) return null;
        ItemStack result = equipment.clone();
        ItemMeta meta = result.getItemMeta();
        if (meta == null) return null;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        int max = pdc.getOrDefault(PDCBuilder.MAX_SLOTS, PersistentDataType.INTEGER, 0);
        if (max >= cap) return null;

        // Item jamais initialisé (trouvé en loot, craft vanilla...) : on crée sa liste de slots
        if (!pdc.has(PDCBuilder.SLOTS, PersistentDataType.LIST.strings())) {
            pdc.set(PDCBuilder.SLOTS, PersistentDataType.LIST.strings(), new ArrayList<>());
            pdc.set(PDCBuilder.USED_SLOTS, PersistentDataType.INTEGER, 0);
        }
        pdc.set(PDCBuilder.MAX_SLOTS, PersistentDataType.INTEGER, Math.min(cap, max + add));
        result.setItemMeta(meta);
        return plugin.pdcBuilder.updateItemLore(result);
    }
}
