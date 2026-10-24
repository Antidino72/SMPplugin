package fr.antidino.smp.modules.perk.type;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.modules.perk.AbstractPerk;
import fr.antidino.smp.core.Utils.ItemBuilder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NoFallPerk extends AbstractPerk {
    public NoFallPerk(String id, CorePlugin plugin, FileConfiguration config) {
        super(id, plugin, config);
    }

    @Override
    public ItemStack onApply(ItemStack item) {
        return new ItemBuilder(item, this.plugin).build();
    }

    @Override
    public void onRemove(Player player, ItemStack item) {

    }

}
