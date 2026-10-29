package fr.antidino.smp.modules.perk;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.Utils.PDCBuilder;
import fr.antidino.smp.modules.perk.cache.PlayerPerkCache;
import fr.antidino.smp.modules.perk.config.ConfigPerkManager;
import fr.antidino.smp.modules.perk.slotExpander.SlotExpander;
import fr.antidino.smp.modules.perk.slotExpander.SlotManager;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PerkManager {
    private final CorePlugin plugin;
    public PerkRegistry perkRegistry;
    public ConfigPerkManager configPerkManager;
    public PlayerPerkCache playerPerkCache;
    private SlotExpander slotExpander;

    public PerkManager(CorePlugin plugin) {
        this.plugin = plugin;
        init();
    }
    public void init(){
        playerPerkCache = new PlayerPerkCache(plugin);
        configPerkManager = new ConfigPerkManager(plugin);
        perkRegistry = new PerkRegistry(plugin,this);
        slotExpander = new SlotExpander(plugin);
    }
    public void reload(){
        if (perkRegistry != null) perkRegistry.reload();
        if (slotExpander != null) slotExpander.reload();
    }
    public SlotExpander getSlotExpander() {
        return slotExpander;
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
        return perkRegistry.registeredPerks.get(id);
    }
    public ArrayList<AbstractPerk> getPerks() {
        return new ArrayList<>(perkRegistry.registeredPerks.values());
    }
    public boolean hasPerk(String id, Player player, EquipmentSlot slot) {
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
    public Set<String> getPerkNames() {
        return  perkRegistry.registeredPerks.keySet();
    }
    public ArrayList<AbstractPerk> getEquippedPerk(Player player, EquipmentSlot slot) {
        List<String> entries = readEntries(getItemInSlot(player, slot));
        if (entries == null) return null;
        ArrayList<AbstractPerk> perks = new ArrayList<>();
        for (String raw : entries) {
            AbstractPerk perk = perkRegistry.registeredPerks.get(PerkEntry.parse(raw).id());
            if (perk != null) perks.add(perk);
        }
        return perks;
    }
}
