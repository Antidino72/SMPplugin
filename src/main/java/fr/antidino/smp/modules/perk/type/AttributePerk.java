package fr.antidino.smp.modules.perk.type;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.modules.perk.AbstractPerk;
import fr.antidino.smp.core.Utils.ItemBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Perk entièrement piloté par le yml : une liste de modificateurs d'attributs
 * (bonus ET malus possibles, ce qui permet de faire des perks avec contrepartie).

 * modifiers:
 *   speed:
 *     attribute: "MOVEMENT_SPEED"
 *     amount: 0.10             # valeur au niveau 1
 *     amount_per_level: 0.05   # ajouté à chaque niveau supplémentaire (peut être négatif)
 *     operation: "ADD_SCALAR"  # ADD_NUMBER / ADD_SCALAR / MULTIPLY_SCALAR_1
 *     slot_group: "FEET"       # ANY / MAINHAND / OFFHAND / HEAD / CHEST / LEGS / FEET / ARMOR ...
 */
public class AttributePerk extends AbstractPerk {
    private static final Logger log = LoggerFactory.getLogger(AttributePerk.class);

    private record Mod(Attribute attribute, double amount, double perLevel,
                       AttributeModifier.Operation operation, EquipmentSlotGroup group) {}

    private final List<Mod> mods = new ArrayList<>();

    public AttributePerk(String id, CorePlugin plugin, FileConfiguration config) {
        super(id, plugin, config);
        loadModifiers(config);
    }

    private void loadModifiers(FileConfiguration config) {
        ConfigurationSection sec = config.getConfigurationSection("modifiers");
        if (sec == null) {
            log.warn("[{}] section 'modifiers' absente du yml", id);
            return;
        }
        for (String name : sec.getKeys(false)) {
            ConfigurationSection m = sec.getConfigurationSection(name);
            if (m == null) continue;

            String attributeName = m.getString("attribute", "");
            Attribute attribute = resolveAttribute(attributeName);
            if (attribute == null) {
                log.warn("[{}] attribut inconnu : {}", id, attributeName);
                continue;
            }

            AttributeModifier.Operation operation = AttributeModifier.Operation.ADD_NUMBER;
            try {
                operation = AttributeModifier.Operation.valueOf(m.getString("operation", "ADD_NUMBER").toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                log.warn("[{}] operation inconnue : {}", id, m.getString("operation"));
            }

            EquipmentSlotGroup group = EquipmentSlotGroup.getByName(m.getString("slot_group", "any").toLowerCase(Locale.ROOT));
            if (group == null) group = EquipmentSlotGroup.ANY;

            mods.add(new Mod(attribute, m.getDouble("amount"), m.getDouble("amount_per_level"), operation, group));
        }
    }

    private static Attribute resolveAttribute(String name) {
        if (name == null || name.isEmpty()) return null;
        String n = name.toLowerCase(Locale.ROOT).replace("generic_", "").replace("player_", "");
        return Registry.ATTRIBUTE.get(NamespacedKey.minecraft(n));
    }

    @Override
    public ItemStack onApply(ItemStack item) {
        return onApply(item, 1);
    }

    @Override
    public ItemStack onApply(ItemStack item, int level) {
        ItemBuilder builder = new ItemBuilder(item, plugin);
        int lvl = Math.max(1, level);
        for (Mod mod : mods) {
            double amount = mod.amount() + mod.perLevel() * (lvl - 1);
            NamespacedKey key = new NamespacedKey(plugin, id + "_" + mod.attribute().getKey().getKey() + "_" + System.nanoTime());
            builder.addAttribute(mod.attribute(), new AttributeModifier(key, amount, mod.operation(), mod.group()));
        }
        return builder.build();
    }

    @Override
    public void onRemove(Player player, ItemStack item) {
    }
}
