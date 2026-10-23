package fr.antidino.smp.modules.listeners;


import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.modules.perk.AbstractPerk;
import fr.antidino.smp.core.Utils.PDCBuilder;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class SmithingListener implements Listener {
    CorePlugin plugin;
    public SmithingListener(CorePlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent event) {

        if (event.getInventory().getInputEquipment() == null) return;
        if (event.getInventory().getInputMineral() == null) return;
        ItemStack equip = event.getInventory().getInputEquipment();
        ItemStack mineral = event.getInventory().getInputMineral();
        ItemMeta mineralMeta = mineral.getItemMeta();
        if (mineralMeta == null) return;
        PersistentDataContainer pdc = mineralMeta.getPersistentDataContainer();


        if (pdc.has(PDCBuilder.SLOT_EXPANDER, PersistentDataType.STRING)) {
            applyExpander(event, equip);
            return;
        }

        if (!(plugin.pdcBuilder.hasEmptySlot(equip))) return;
        String perk_id = pdc.get(PDCBuilder.PERK_ID, PersistentDataType.STRING);
        AbstractPerk perk = plugin.perkManager.getPerk(perk_id);
        if (perk == null) return;
        int level = Math.min(plugin.pdcBuilder.getPerkLevel(mineral), perk.getMaxLevel());
        apply_perk(event,equip,perk_id,level);

    }
    private void apply_perk(PrepareSmithingEvent event,ItemStack equip ,String perk_id,int level){
        ItemStack resultItem = equip.clone();
        resultItem = plugin.slotManager.applyPerkToNextAvailableSlot(resultItem,perk_id,level);
        plugin.perkManager.getPerk(perk_id).onApply(resultItem,level);
        event.setResult(resultItem);
    }
    private void applyExpander(PrepareSmithingEvent event, ItemStack equip) {
        if (plugin.slotExpander == null || !plugin.slotExpander.isSupported(equip.getType())) return;
        ItemStack result = plugin.slotManager.expandSlots(equip, plugin.slotExpander.getAdded(), plugin.slotExpander.getMax());
        if (result == null) return; // déjà au maximum de slots
        event.setResult(result);
    }
}
