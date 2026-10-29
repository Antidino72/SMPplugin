package fr.antidino.smp.modules.perk;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;

public class PerkLevelManager {
    /** Niveau (le plus haut) d'un perk sur l'item porté dans ce slot, 0 s'il n'y est pas. */
    /*
    public int getPerkLevel(Player player, EquipmentSlot slot, String id) {
        List<String> entries = readEntries(getItemInSlot(player, slot));
        if (entries == null) return 0;
        int best = 0;
        for (String raw : entries) {
            PerkEntry entry = PerkEntry.parse(raw);
            if (entry.id().equals(id)) best = Math.max(best, entry.level());
        }
        return best;
    }*/
}
