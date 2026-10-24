package fr.antidino.smp.modules.spell;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.Utils.PDCBuilder;
import fr.antidino.smp.modules.perk.AbstractPerk;
import fr.antidino.smp.modules.spell.type.FlameSpell;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class SpellManager {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private final Map<String, AbstractSpell> registeredSpells = new HashMap<>();
    private final CorePlugin plugin;
    private final SpellConfig spellConfig;


    public SpellManager(CorePlugin plugin) {
        this.plugin = plugin;
        this.spellConfig = new SpellConfig(plugin);
        registerDefaults();
    }

    private void registerDefaults() {
            registerSpell(new FlameSpell("flame","Flamme",spellConfig.FlameConfiguration,plugin));
    }

    /** Recharge tous les perks depuis les yml (crafts et crafts d'amélioration inclus). */
    public void reload() {
        registeredSpells.clear();
        registerDefaults();

    }

    public void registerSpell(AbstractSpell spell) {
        registeredSpells.put(spell.getId(), spell);
    }

    public String getPerkId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = PDCBuilder.PERK_ID;
        if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        }
        return null;
    }

    public AbstractSpell getSpell(String id) {
        return registeredSpells.get(id);
    }
    public ArrayList<AbstractSpell> getSpells() {
        return new ArrayList<>(registeredSpells.values());
    }
   
    
}
