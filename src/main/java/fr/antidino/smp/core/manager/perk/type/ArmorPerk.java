package fr.antidino.smp.core.manager.perk.type;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.manager.perk.AbstractPerk;
import fr.antidino.smp.librairie.ItemBuilder;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;

public class ArmorPerk extends AbstractPerk {

    public ArmorPerk(String id, CorePlugin plugin, FileConfiguration config) {
        super(id, plugin,config);
    }

    @Override
    protected int defaultMaxLevel() {
        return 3;
    }

    @Override
    public ItemStack onApply(ItemStack item) {
        return onApply(item, 1);
    }

    @Override
    public ItemStack onApply(ItemStack item, int level) {
        double m = getMultiplier(level);
        return new ItemBuilder(item,this.plugin)

                .addAttribute(
                        Attribute.ARMOR_TOUGHNESS,
                        1.5 * m,
                        AttributeModifier.Operation.ADD_SCALAR,
                        EquipmentSlotGroup.ARMOR
                )
                .addAttribute(
                        Attribute.ARMOR,
                        1.3 * m,
                        AttributeModifier.Operation.ADD_SCALAR,
                        EquipmentSlotGroup.ARMOR
                )

                .build();

    }

    @Override
    public void onRemove(Player player, ItemStack item) {

    }
}
