package fr.antidino.smp.modules.perk.type;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.modules.perk.AbstractPerk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Perk sans modification d'attribut : l'effet est géré par
 * PerkTasks / PerkEffectListener (double saut, vision nocturne, vampire, aimant, fonte...).
 * Les valeurs par niveau se règlent dans la section "values" du yml.
 */
public class PassivePerk extends AbstractPerk {

    public PassivePerk(String id, CorePlugin plugin, FileConfiguration config) {
        super(id, plugin, config);
    }

    @Override
    public ItemStack onApply(ItemStack item) {
        return item;
    }

    @Override
    public void onRemove(Player player, ItemStack item) {
    }
}
