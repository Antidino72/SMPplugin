package fr.antidino.smp.librairie;

import fr.antidino.smp.CorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ItemBuilder {
    private final ItemStack item;
    private CorePlugin plugin;
    public ItemBuilder(Material material, CorePlugin plugin) {

        this.item = new ItemStack(material);
        this.plugin = plugin;
    }

    public ItemBuilder(ItemStack item, CorePlugin plugin) {
        this.item = item;
        this.plugin = plugin;
    }

    public ItemBuilder setName(String name) {
        ItemMeta meta = this.item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name));
            this.item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder setLore(List<Component> lore) {
        ItemMeta meta = this.item.getItemMeta();
        if (meta != null) {
            meta.lore(lore);
            this.item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder addAttribute(Attribute attribute, AttributeModifier modifier) {
        ItemMeta meta = this.item.getItemMeta();
        if (meta != null) {
            meta.addAttributeModifier(attribute, modifier);
            this.item.setItemMeta(meta);
        }
        return this;
    }
    public ItemBuilder addAttribute(Attribute attribute, double amount, AttributeModifier.Operation operation, EquipmentSlotGroup slotGroup) {
        ItemMeta meta = this.item.getItemMeta();
        if (meta != null) {
               NamespacedKey key = new NamespacedKey(plugin, attribute.getKey().getKey() + "_" + System.currentTimeMillis());

            AttributeModifier modifier = new AttributeModifier(key, amount, operation, slotGroup);
            meta.addAttributeModifier(attribute, modifier);
            this.item.setItemMeta(meta);
        }
        return this;
    }
    public ItemBuilder addPDC(NamespacedKey key,String type) {
        ItemMeta meta = this.item.getItemMeta();
        meta.getPersistentDataContainer().set(key,PersistentDataType.STRING,type);
        item.setItemMeta(meta);
        return this;
    }
    public static String getPDC(ItemStack item,NamespacedKey key,String type) {
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(key,PersistentDataType.STRING);
    }


    public ItemStack build() {
        return this.item;
    }
}