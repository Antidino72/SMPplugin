package fr.antidino.smp.modules.perk;


import org.bukkit.inventory.EquipmentSlot;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerksCache {
    private final Map<EquipmentSlot, List<String>> perks = new HashMap<>();

    public Map<EquipmentSlot,List<String>> get() {
        return perks;
    }


    public void set(EquipmentSlot slot, List<String> perks_list) {
        perks.put(slot, perks_list);
    }



}
