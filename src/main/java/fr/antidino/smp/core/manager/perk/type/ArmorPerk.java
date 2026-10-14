package fr.antidino.smp.core.manager.perk.type;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.manager.perk.AbstractPerk;
import fr.antidino.smp.librairie.ItemBuilder;
import fr.antidino.smp.librairie.PDCBuilder;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;

public class ArmorPerk extends AbstractPerk {

    public ArmorPerk(String id, String displayName, CorePlugin plugin) {
        super(id, displayName, plugin,"armor_perk",new ItemBuilder(Material.GOLD_INGOT,plugin).addPDC(PDCBuilder.PERK_ID,id).setName("Armure Supplémentaire").build());
    }
    @Override
    public ItemStack onApply(ItemStack item) {
        return new ItemBuilder(item,this.plugin)

                .addAttribute(
                        Attribute.ARMOR_TOUGHNESS,
                        1.5,
                        AttributeModifier.Operation.ADD_SCALAR,
                        EquipmentSlotGroup.ARMOR
                )
                .addAttribute(
                        Attribute.ARMOR,
                        1.3,
                        AttributeModifier.Operation.ADD_SCALAR,
                        EquipmentSlotGroup.ARMOR
                )

                .build();

    }

    @Override
    public void onRemove(Player player, ItemStack item) {

    }
}
