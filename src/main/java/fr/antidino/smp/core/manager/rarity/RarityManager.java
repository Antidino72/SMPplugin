package fr.antidino.smp.core.manager.rarity;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.manager.perk.AbstractPerk;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Charge rarity.yml : des niveaux de rareté (
 * auxquels on associe des perks par id.
 */
public class RarityManager {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final CorePlugin plugin;

    private final Map<String, Rarity> rarities = new LinkedHashMap<>();
    private final Map<String, Rarity> perkRarity = new LinkedHashMap<>();
    private Rarity defaultRarity;
    private boolean colorPerkName = true;
    private boolean sortCodex = true;
    private String rarityLine = "<gray>Rareté : %rarity%";

    public RarityManager(CorePlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "rarity.yml");
        if (!file.exists()) plugin.saveResource("rarity.yml", false);
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        rarities.clear();
        perkRarity.clear();
        defaultRarity = null;

        colorPerkName = config.getBoolean("color_perk_name", true);
        sortCodex = config.getBoolean("sort_codex_by_rarity", true);
        rarityLine = config.getString("rarity_line", "<gray>Rareté : %rarity%");

        ConfigurationSection sec = config.getConfigurationSection("rarities");
        if (sec != null) {
            int order = 0;
            for (String id : sec.getKeys(false)) {
                ConfigurationSection r = sec.getConfigurationSection(id);
                if (r == null) continue;
                Rarity rarity = new Rarity(
                        id,
                        r.getString("display_name", id),
                        r.getString("color", "<white>"),
                        r.getBoolean("color_name", colorPerkName),
                        order++);
                rarities.put(id.toLowerCase(Locale.ROOT), rarity);

                for (String perkId : r.getStringList("perks")) {
                    String key = normalize(perkId);
                    Rarity previous = perkRarity.putIfAbsent(key, rarity);
                    if (previous != null && previous != rarity) {
                        plugin.getLogger().warning("[rarity] le perk '" + perkId + "' est dans '"
                                + previous.id() + "' et '" + id + "' : '" + previous.id() + "' est gardée.");
                    }
                }
            }
        } else {
            plugin.getLogger().warning("[rarity] section 'rarities' absente de rarity.yml");
        }

        String def = config.getString("default_rarity", "");
        if (def != null && !def.isBlank()) {
            defaultRarity = rarities.get(def.toLowerCase(Locale.ROOT));
            if (defaultRarity == null) {
                plugin.getLogger().warning("[rarity] default_rarity '" + def + "' n'existe pas.");
            }
        }
    }

    private static String normalize(String id) {
        String s = id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
        return s.endsWith("_perk") ? s.substring(0, s.length() - 5) : s;
    }

    public Rarity getRarity(String perkId) {
        Rarity r = perkRarity.get(normalize(perkId));
        return r != null ? r : defaultRarity;
    }

    public Rarity getRarity(AbstractPerk perk) {
        return perk == null ? null : getRarity(perk.getId());
    }

    public Component styledName(AbstractPerk perk) {
        Rarity rarity = getRarity(perk);
        if (rarity == null || !rarity.colorName()) {
            return MM.deserialize("<!italic>" + perk.getDisplayName());
        }
        String plain = MM.stripTags(perk.getDisplayName());
        return MM.deserialize("<!italic>" + rarity.color() + plain);
    }

    public Component rarityLine(AbstractPerk perk) {
        Rarity rarity = getRarity(perk);
        if (rarity == null) return null;
        return MM.deserialize("<!italic>" + rarityLine.replace("%rarity%", rarity.labelMini()));
    }

    public boolean sortCodexByRarity() {
        return sortCodex;
    }

    public int order(AbstractPerk perk) {
        Rarity r = getRarity(perk);
        return r == null ? -1 : r.order();
    }
}
