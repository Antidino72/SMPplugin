package fr.antidino.smp.librairie;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.manager.perk.AbstractPerk;
import fr.antidino.smp.core.manager.perk.PerkEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PDCBuilder {
    private static final Logger log = LoggerFactory.getLogger(PDCBuilder.class);
    public static NamespacedKey MAX_SLOTS;
    public static NamespacedKey USED_SLOTS;
    public static NamespacedKey SLOTS;
    public static NamespacedKey PERK_ID;
    public static NamespacedKey PERK_LEVEL;
    public static NamespacedKey SLOT_EXPANDER;

    private final CorePlugin plugin;
    public PDCBuilder(CorePlugin plugin) {
        this.plugin = plugin;
    }
    public static void init(CorePlugin plugin) {

        MAX_SLOTS = new NamespacedKey(plugin, "max_slots");
        USED_SLOTS = new NamespacedKey(plugin, "used_slots");
        PERK_ID = new NamespacedKey(plugin,"perk_id");
        SLOTS = new NamespacedKey(plugin, "perk_slots");
        PERK_LEVEL = new NamespacedKey(plugin, "perk_level");
        SLOT_EXPANDER = new NamespacedKey(plugin, "slot_expander");

    }
    public ItemStack initializeSlots(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return item;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();



        int maxSlots = ThreadLocalRandom.current().nextInt(plugin.getConfig().getInt("slot.min"), plugin.getConfig().getInt("slot.max")+1);
        pdc.set(SLOTS,PersistentDataType.LIST.strings(),new ArrayList<>());
        pdc.set(MAX_SLOTS, PersistentDataType.INTEGER, maxSlots);
        pdc.set(USED_SLOTS, PersistentDataType.INTEGER, 0);
        item.setItemMeta(meta);
        item  = updateItemLore(item);
        log.info(item.toString());
        return item;
    }

    /**
     * Permet de savoir si l'item possède encore un emplacement libre.
     */
    public boolean hasEmptySlot(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();

        int max = pdc.getOrDefault(MAX_SLOTS, PersistentDataType.INTEGER, 0);
        int used = pdc.getOrDefault(USED_SLOTS, PersistentDataType.INTEGER, 0);

        return used < max;
    }


    public ArrayList<String> getAppliedPerks(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return new ArrayList<>();

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        List<String> perksArray = pdc.get(SLOTS, PersistentDataType.LIST.strings());

        if (perksArray == null) {
            plugin.getLogger().warning("Perks list is null");
            return new ArrayList<>();
        }

        return  new ArrayList<>(perksArray);
    }
    public ItemStack addPerkToItem(ItemStack item, String perkId) {
        if (item == null || !item.hasItemMeta()) return item;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();


        List<String> perks = getAppliedPerks(item);


        perks.add(perkId);

        pdc.set(SLOTS, PersistentDataType.LIST.strings(), perks);

        pdc.set(USED_SLOTS, PersistentDataType.INTEGER, perks.size());

        item.setItemMeta(meta);


        return updateItemLore(item);
    }
    public ItemStack updateItemLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            plugin.getLogger().warning("[SMPplugin] Item is null");
            return item;
        }

        List<String> perks = getAppliedPerks(item);
        int max = meta.getPersistentDataContainer().getOrDefault(MAX_SLOTS, PersistentDataType.INTEGER, 0);
        List<Component> lore = new ArrayList<>();

        lore.add(MiniMessage.miniMessage().deserialize("<!italic><gray>========================"));
        lore.add(MiniMessage.miniMessage().deserialize("<!italic><gray>Améliorations <dark_gray>(<white>" + perks.size() + "<dark_gray>/<white>" + max + "<dark_gray>) <gray>:"));

        if (perks.isEmpty()) {
            lore.add(MiniMessage.miniMessage().deserialize("<!italic><dark_gray> Aucun effet appliqué"));
        } else {
            for (String raw : perks) {
                PerkEntry entry = PerkEntry.parse(raw);
                AbstractPerk perk = plugin.perkManager == null ? null : plugin.perkManager.getPerk(entry.id());
                String name = perk != null ? perk.getDisplayName() : entry.id();
                String level = entry.level() > 1 ? " <gray>" + AbstractPerk.roman(entry.level()) : "";
                lore.add(MiniMessage.miniMessage().deserialize("<!italic><green> ▪ <white>" + name + level));
            }
        }

        lore.add(MiniMessage.miniMessage().deserialize("<!italic><gray>========================"));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /** Niveau d'un item perk (1 si absent : anciens items et niveau 1). */
    public int getPerkLevel(ItemStack perkItem) {
        if (perkItem == null || !perkItem.hasItemMeta()) return 1;
        return Math.max(1, perkItem.getItemMeta().getPersistentDataContainer()
                .getOrDefault(PERK_LEVEL, PersistentDataType.INTEGER, 1));
    }

}
