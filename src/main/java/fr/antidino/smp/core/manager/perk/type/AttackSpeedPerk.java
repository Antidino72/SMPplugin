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

public class AttackSpeedPerk extends AbstractPerk {


    public AttackSpeedPerk(String id, CorePlugin plugin, FileConfiguration config) {
        super(id, plugin,config);
    }


    @Override
    public ItemStack onApply(ItemStack item) {
        return new ItemBuilder(item,this.plugin)
                .addAttribute(
                        Attribute.ATTACK_SPEED,
                        1.2,
                        AttributeModifier.Operation.ADD_SCALAR,
                        EquipmentSlotGroup.MAINHAND
                )
                .build();

    }

    @Override
    public void onRemove(Player player, ItemStack item) {

    }
}


