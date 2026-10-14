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

public class HealthPerk extends AbstractPerk {


    public HealthPerk(String id, String displayName, CorePlugin plugin) {
        super(id, displayName, plugin,"health_perk",new ItemBuilder(Material.REDSTONE,plugin).addPDC(PDCBuilder.PERK_ID,id).setName("Vie Supplémentaire").build());
    }


    @Override
    public ItemStack onApply(ItemStack item) {
        return new ItemBuilder(item,this.plugin)
                .addAttribute(
                        Attribute.MAX_HEALTH,
                        2,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.ARMOR
                )

                .build();

    }

    @Override
    public void onRemove(Player player, ItemStack item) {

    }
}
