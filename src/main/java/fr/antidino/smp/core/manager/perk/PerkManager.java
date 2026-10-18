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
    public ArrayList<AbstractPerk> getEquippedPerk(Player player, EquipmentSlot slot) {
            ItemStack item = null;

           switch (slot) {
               case LEGS ->  item = player.getInventory().getLeggings();
               case FEET ->  item = player.getInventory().getBoots();
               case HEAD ->  item = player.getInventory().getHelmet();
               case CHEST ->  item = player.getInventory().getChestplate();
               case OFF_HAND -> item = player.getInventory().getItemInOffHand();
               case HAND ->   item = player.getInventory().getItemInMainHand();
           }
            if (item == null || !item.hasItemMeta())  return null;

            ItemMeta meta = item.getItemMeta();
            NamespacedKey key = PDCBuilder.SLOTS;
            ArrayList<AbstractPerk> perks = new ArrayList<>();
            if (meta.getPersistentDataContainer().has(key, PersistentDataType.LIST.strings())) {
                List<String> perkId = meta.getPersistentDataContainer().get(key, PersistentDataType.LIST.strings());
                perkId.forEach(id -> {
                    perks.add(registeredPerks.get(id));
                });
                return perks;
            }
            return null;
    }
}