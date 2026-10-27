package fr.antidino.smp.modules.perk.cache;


import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.Utils.PDCBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;

public class PlayerPerkCache {
    private static final Logger log = LoggerFactory.getLogger(PlayerPerkCache.class);
    private final CorePlugin plugin;

    private final Map<UUID,PerksCache> cache = new HashMap<>();

    public PlayerPerkCache(CorePlugin plugin) {
        this.plugin = plugin;
    }
    /** Clean Cache Perk **/
    public void cleanup(Player player) {
        cache.remove(player.getUniqueId());
    }
    /** Clean Cache Perk **/
    public void cleanup(UUID uuid) {
        cache.remove(uuid);
    }
    public Map<UUID,PerksCache> getCache() {
        return cache;
    }
    public PerksCache getPerksCache(UUID uuid) {
        return cache.get(uuid);
    }

    public void update(Player player) {
        PerksCache perksCache = cache.get(player.getUniqueId());
        if (perksCache == null) {
            perksCache =  new PerksCache();
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack item = getItemInSlot(player, slot);
            List<String> perks = readEntries(item);
            perksCache.set(slot, perks);
        }
        cache.put(player.getUniqueId(), perksCache);
        log.info("PlayerPerkCache updated");

    }
    public void updateSlot(Player player, EquipmentSlot slot) {
        PerksCache perksCache = cache.get(player.getUniqueId());
        if (perksCache == null) {
            perksCache =  new PerksCache();
        }
        ItemStack item = getItemInSlot(player, slot);
        List<String> perks = readEntries(item);
        perksCache.set(slot, perks);
        cache.put(player.getUniqueId(), perksCache);
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

}