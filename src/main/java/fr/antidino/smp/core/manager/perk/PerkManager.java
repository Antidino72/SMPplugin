package fr.antidino.smp.core.manager.perk;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.manager.perk.config.ConfigPerkManager;
import fr.antidino.smp.core.manager.perk.type.*;
import fr.antidino.smp.librairie.PDCBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerkManager {
    private static final Logger log = LoggerFactory.getLogger(PerkManager.class);
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private final Map<String, AbstractPerk> registeredPerks = new HashMap<>();
    private final CorePlugin plugin;


    public PerkManager(CorePlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(new PerkListenner(plugin),plugin);
        registerDefaults();
    }

    private void registerDefaults() {

        ConfigPerkManager configPerkManager = plugin.configPerkManager;
        registerPerk(new HealthPerk(PerkTypeId.HEALTH,plugin,configPerkManager.healthConfig));
        registerPerk(new ArmorPerk(PerkTypeId.ARMOR,plugin,configPerkManager.ArmorConfig));
        registerPerk(new SpeedPerk(PerkTypeId.SPEED,plugin,configPerkManager.SpeedConfig));
        registerPerk(new AttackSpeedPerk(PerkTypeId.ATTACK_SPEED,plugin,configPerkManager.AttackSpeedConfig));
        registerPerk(new AttackDamagePerk(PerkTypeId.ATTACK_DAMAGE,plugin,configPerkManager.AttackDamageConfig));
        registerPerk(new NoFallPerk(PerkTypeId.NO_FALL_PERK,plugin,configPerkManager.NoFallConfig));
        registerPerk(new WardenPerk(PerkTypeId.WARDEN_PERK,plugin,configPerkManager.WardenConfig));

        // Nouveaux perks (effets gérés par PerkTasks / PerkEffectListener)
        registerPerk(new PassivePerk(PerkTypeId.DOUBLE_JUMP, plugin, configPerkManager.get("double_jump.yml")));
        registerPerk(new PassivePerk(PerkTypeId.NIGHT_VISION, plugin, configPerkManager.get("night_vision.yml")));
        registerPerk(new PassivePerk(PerkTypeId.VAMPIRE, plugin, configPerkManager.get("vampire.yml")));
        registerPerk(new PassivePerk(PerkTypeId.MAGNET, plugin, configPerkManager.get("magnet.yml")));
        registerPerk(new PassivePerk(PerkTypeId.SMELTER, plugin, configPerkManager.get("smelter.yml")));

        // Perks pilotés par une liste de modificateurs d'attributs dans le yml (bonus + malus)
        registerPerk(new AttributePerk(PerkTypeId.MINER, plugin, configPerkManager.get("miner.yml")));
        registerPerk(new AttributePerk(PerkTypeId.SPRINTER, plugin, configPerkManager.get("sprinter.yml")));
        registerPerk(new AttributePerk(PerkTypeId.GLASS_CANNON, plugin, configPerkManager.get("glass_cannon.yml")));
    }

    /** Recharge tous les perks depuis les yml (crafts et crafts d'amélioration inclus). */
    public void reload() {
        registeredPerks.values().forEach(AbstractPerk::unregisterRecipes);
        registeredPerks.clear();
        registerDefaults();
    }

    public void registerPerk(AbstractPerk perk) {
        registeredPerks.put(perk.getId(), perk);
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

    public AbstractPerk getPerk(String id) {
        return registeredPerks.get(id);
    }
    public ArrayList<AbstractPerk> getPerks() {
        return new ArrayList<>(registeredPerks.values());
    }
    public boolean hasPerk(String id,Player player,EquipmentSlot slot) {
        ArrayList<AbstractPerk> perks = getEquippedPerk(player,slot);
        if (perks == null) return false;
        return perks.contains(getPerk(id));
    }

    private ItemStack getItemInSlot(Player player, EquipmentSlot slot) {
        return switch (slot) {
            case LEGS -> player.getInventory().getLeggings();
            case FEET -> player.getInventory().getBoots();
            case HEAD -> player.getInventory().getHelmet();
            case CHEST -> player.getInventory().getChestplate();
            case OFF_HAND -> player.getInventory().getItemInOffHand();
            case HAND -> player.getInventory().getItemInMainHand();
            default -> null;
        };
    }

    private List<String> readEntries(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(PDCBuilder.SLOTS, PersistentDataType.LIST.strings());
    }

    public ArrayList<AbstractPerk> getEquippedPerk(Player player, EquipmentSlot slot) {
        List<String> entries = readEntries(getItemInSlot(player, slot));
        if (entries == null) return null;
        ArrayList<AbstractPerk> perks = new ArrayList<>();
        for (String raw : entries) {
            AbstractPerk perk = registeredPerks.get(PerkEntry.parse(raw).id());
            if (perk != null) perks.add(perk);
        }
        return perks;
    }

    /** Niveau (le plus haut) d'un perk sur l'item porté dans ce slot, 0 s'il n'y est pas. */
    public int getPerkLevel(Player player, EquipmentSlot slot, String id) {
        List<String> entries = readEntries(getItemInSlot(player, slot));
        if (entries == null) return 0;
        int best = 0;
        for (String raw : entries) {
            PerkEntry entry = PerkEntry.parse(raw);
            if (entry.id().equals(id)) best = Math.max(best, entry.level());
        }
        return best;
    }

    /** Niveau le plus haut de chaque perk présent sur les 4 pièces d'armure (id -> niveau). */
    public Map<String, Integer> getArmorPerkLevels(Player player) {
        Map<String, Integer> levels = new HashMap<>();
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            List<String> entries = readEntries(getItemInSlot(player, slot));
            if (entries == null) continue;
            for (String raw : entries) {
                PerkEntry entry = PerkEntry.parse(raw);
                levels.merge(entry.id(), entry.level(), Math::max);
            }
        }
        return levels;
    }
}
