package fr.antidino.smp.core.listenner;


import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.manager.perk.AbstractPerk;
import fr.antidino.smp.librairie.PDCBuilder;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
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
        if (!(plugin.pdcBuilder.hasEmptySlot(equip))) return;
        String perk_id = mineral.getItemMeta().getPersistentDataContainer().get(PDCBuilder.PERK_ID, PersistentDataType.STRING);
        AbstractPerk perk = plugin.perkManager.getPerk(perk_id);
        if (perk == null) return;
        apply_perk(event,equip,perk_id);
    }
    private void apply_perk(PrepareSmithingEvent event,ItemStack equip ,String perk_id){
        ItemStack resultItem = equip.clone();
        resultItem = plugin.slotManager.applyPerkToNextAvailableSlot(resultItem,perk_id);
        plugin.perkManager.getPerk(perk_id).onApply(resultItem);
        event.setResult(resultItem);
    }
}