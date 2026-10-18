
package fr.antidino.smp.core.manager.perk.type;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.manager.perk.AbstractPerk;
import fr.antidino.smp.librairie.ItemBuilder;
import fr.antidino.smp.librairie.PDCBuilder;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;

public class SpeedPerk extends AbstractPerk {


    public SpeedPerk(String id, CorePlugin plugin, FileConfiguration config) {
        super(id,plugin, config);
    }


    @Override
    public ItemStack onApply(ItemStack item) {
        return new ItemBuilder(item, this.plugin)
                .addAttribute(
                        Attribute.MOVEMENT_SPEED,
                        1.3,
                        AttributeModifier.Operation.ADD_SCALAR,
                        EquipmentSlotGroup.FEET
                )
                .addAttribute(
                        Attribute.SNEAKING_SPEED,
                        1.1,
                        AttributeModifier.Operation.ADD_SCALAR,
                        EquipmentSlotGroup.FEET
                )
                .build();

    }

    @Override
    public void onRemove(Player player, ItemStack item) {

    }
}
