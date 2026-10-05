package fr.antidino.smp.librairie;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ItemBuilder {
    private final ItemStack item;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
    }

    public ItemBuilder(ItemStack item) {
        this.item = item;
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

    public ItemBuilder setPdcData(NamespacedKey key, String value) {
        ItemMeta meta = this.item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
            this.item.setItemMeta(meta);
        }
        return this;
    }

    public String getPdcData(NamespacedKey key) {
        ItemMeta meta = this.item.getItemMeta();
        if (meta != null) {
            return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        }
        return null;
    }

    public ItemStack build() {
        return this.item;
    }
}