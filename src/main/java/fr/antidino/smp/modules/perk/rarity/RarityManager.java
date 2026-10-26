package fr.antidino.smp.modules.perk.rarity;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.modules.perk.AbstractPerk;
import fr.antidino.smp.modules.perk.PerkEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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
    private boolean symbolInName = false;
    private boolean sortCodex = true;
    private String rarityLine = "<gray>Rareté : %rarity%";
    private String limitMessage = "<red>Cet équipement a déjà %max% perk(s) %rarity%<red> !";

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
        symbolInName = config.getBoolean("symbol_in_name", false);
        sortCodex = config.getBoolean("sort_codex_by_rarity", true);
        rarityLine = config.getString("rarity_line", "<gray>Rareté : %rarity%");
        limitMessage = config.getString("limit_message",
                "<red>Cet équipement a déjà %max% perk(s) %rarity%<red> !");

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
                        r.getString("symbol", ""),
                        r.getBoolean("color_name", colorPerkName),
                        r.getInt("max_per_item", -1),
                        r.getString("craft_announce", ""),
                        r.getString("craft_sound", ""),
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

        // Vérification des ids une fois tous les perks chargés (au prochain tick)
        Bukkit.getScheduler().runTask(plugin, this::validate);
    }

    /** Avertit dans la console si rarity.yml cite un perk qui n'existe pas. */
    private void validate() {
        if (plugin.perkManager == null) return;
        for (String key : perkRarity.keySet()) {
            boolean found = plugin.perkManager.getPerks().stream()
                    .anyMatch(p -> normalize(p.getId()).equals(key));
            if (!found) {
                plugin.getLogger().warning("[rarity] perk inconnu dans rarity.yml : '" + key
                        + "' (voir les ids avec /perk give)");
            }
        }
    }

    private static String normalize(String id) {
        String s = id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
        return s.endsWith("_perk") ? s.substring(0, s.length() - 5) : s;
    }

    // ------------------------------------------------------------ LECTURE

    public Collection<Rarity> getRarities() {
        return Collections.unmodifiableCollection(rarities.values());
    }

    /** Rareté d'une perk (celle par défaut s'il n'est listé nulle part, null si aucune). */
    public Rarity getRarity(String perkId) {
        Rarity r = perkRarity.get(normalize(perkId));
        return r != null ? r : defaultRarity;
    }

    public Rarity getRarity(AbstractPerk perk) {
        return perk == null ? null : getRarity(perk.getId());
    }

    /** Nom du perk (sans italique), coloré selon sa rareté si activé dans rarity.yml. */
    public Component styledName(AbstractPerk perk) {
        Rarity rarity = getRarity(perk);
        if (rarity == null || !rarity.colorName()) {
            return MM.deserialize("<!italic>" + perk.getDisplayName());
        }
        String symbol = symbolInName && !rarity.symbol().isEmpty() ? rarity.symbol() + " " : "";
        String plain = MM.stripTags(perk.getDisplayName());
        return MM.deserialize("<!italic>" + rarity.color() + symbol + plain);
    }

    /** Ligne "Rareté : X" pour le codex, ou null si le perk n'a pas de rareté. */
    public Component rarityLine(AbstractPerk perk) {
        Rarity rarity = getRarity(perk);
        if (rarity == null) return null;
        return MM.deserialize("<!italic>" + rarityLine.replace("%rarity%", rarity.labelMini()));
    }

    public boolean sortCodexByRarity() {
        return sortCodex;
    }

    /** Ordre de tri : plus la rareté est haute, plus le nombre est grand (sans rareté = -1). */
    public int order(AbstractPerk perk) {
        Rarity r = getRarity(perk);
        return r == null ? -1 : r.order();
    }

    // ------------------------------------------------------------ LIMITES

    /**
     * Vérifie la limite "max_per_item" de la rareté du perk sur cet équipement.
     * Envoie le message "limit_message" au joueur (si non null) quand c'est refusé.
     */
    public boolean canApply(Player viewer, ItemStack equipment, AbstractPerk perk) {
        Rarity rarity = getRarity(perk);
        if (rarity == null || !rarity.hasLimit()) return true;

        int count = 0;
        List<String> applied = plugin.pdcBuilder.getAppliedPerks(equipment);
        for (String raw : applied) {
            AbstractPerk other = plugin.perkManager.getPerk(PerkEntry.parse(raw).id());
            if (other != null && getRarity(other) == rarity) count++;
        }
        if (count < rarity.maxPerItem()) return true;

        if (viewer != null) {
            viewer.sendMessage(MM.deserialize(limitMessage
                    .replace("%max%", String.valueOf(rarity.maxPerItem()))
                    .replace("%rarity%", rarity.labelMini())));
        }
        return false;
    }

    // ------------------------------------------------------------ CRAFT

    /** Annonce + son quand un joueur craft un perk dont la rareté en définit un. */
    public void onCraft(Player player, AbstractPerk perk, int level) {
        Rarity rarity = getRarity(perk);
        if (rarity == null) return;

        if (!rarity.announce().isBlank()) {
            String plain = MM.stripTags(perk.getDisplayName());
            String text = rarity.announce()
                    .replace("%player%", player.getName())
                    .replace("%perk%", rarity.color() + plain)
                    .replace("%level%", AbstractPerk.roman(Math.max(1, level)))
                    .replace("%rarity%", rarity.labelMini());
            Bukkit.broadcast(MM.deserialize(text));
        }
        if (!rarity.sound().isBlank()) {
            player.playSound(player.getLocation(), rarity.sound(), 1.0f, 1.0f);
        }
    }
}
