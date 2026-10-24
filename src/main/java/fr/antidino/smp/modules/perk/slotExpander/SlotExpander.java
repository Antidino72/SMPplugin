package fr.antidino.smp.modules.perk.slotExpander;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.modules.perk.SmithingRegisterAllItem;
import fr.antidino.smp.core.Utils.ItemBuilder;
import fr.antidino.smp.core.Utils.PDCBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.persistence.PersistentDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Item craftable qui ajoute des emplacements de perks à un équipement
 * (armure, armes, outils...). Se configure dans plugins/SMPplugin/slot_expander.yml.
 * Utilisation en jeu : table de forge, équipement en haut + l'item dans la case du bas.
 */
public class SlotExpander {
    private static final Logger log = LoggerFactory.getLogger(SlotExpander.class);

    private final CorePlugin plugin;
    private final NamespacedKey key;

    private String displayName = "<light_purple>Cristal d'emplacement";
    private ItemStack item;
    private int added = 1;
    private int max = 6;
    private List<String> description = List.of();
    private List<String> craftShape = List.of();
    private Map<Character, Material> craftIngredients = Map.of();
    private final Set<Material> supported = new HashSet<>();

    public SlotExpander(CorePlugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "slot_expander");
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "slot_expander.yml");
        if (!file.exists()) plugin.saveResource("slot_expander.yml", false);
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        displayName = config.getString("item.display_name", "<light_purple>Cristal d'emplacement");
        added = Math.max(1, config.getInt("slots.added", 1));
        max = Math.max(1, config.getInt("slots.max", 6));
        description = Collections.unmodifiableList(new ArrayList<>(config.getStringList("description")));
        craftShape = Collections.unmodifiableList(new ArrayList<>(config.getStringList("craft.shape")));

        Map<Character, Material> ingredients = new LinkedHashMap<>();
        ConfigurationSection sec = config.getConfigurationSection("craft.ingredient");
        if (sec != null) {
            for (String k : sec.getKeys(false)) {
                if (k.isEmpty()) continue;
                Material m = Material.matchMaterial(sec.getString(k, ""));
                if (m == null) {
                    log.warn("[slot_expander] matériau inconnu pour '{}' : {}", k, sec.getString(k));
                    continue;
                }
                ingredients.put(k.charAt(0), m);
            }
        }
        craftIngredients = Collections.unmodifiableMap(ingredients);

        // Équipements acceptés : armures/armes/outils du plugin + liste "slots.extra_items" du yml
        supported.clear();
        supported.addAll(SmithingRegisterAllItem.getAllMaterials());
        for (String name : config.getStringList("slots.extra_items")) {
            Material m = Material.matchMaterial(name);
            if (m != null) supported.add(m);
        }

        Material material = Material.matchMaterial(config.getString("item.material", "AMETHYST_SHARD"));
        if (material == null) material = Material.AMETHYST_SHARD;

        List<Component> lore = new ArrayList<>();
        for (String line : description) lore.add(mm(line));
        lore.add(Component.empty());
        lore.add(mm("<gray>Table de forge : <white>équipement + cet item"));
        lore.add(mm("<gray>Ajoute <white>+" + added + " <gray>emplacement(s) <dark_gray>(max " + max + ")"));

        item = new ItemBuilder(material, plugin)
                .addPDC(PDCBuilder.SLOT_EXPANDER, "1")
                .setName(mm(displayName))
                .setLore(lore)
                .build();

        registerRecipe();
    }

    private void registerRecipe() {
        Bukkit.removeRecipe(key);
        if (craftShape.isEmpty()) {
            log.warn("[slot_expander] aucun craft défini (craft.shape)");
            return;
        }
        try {
            ShapedRecipe recipe = new ShapedRecipe(key, item.clone());
            recipe.shape(craftShape.toArray(new String[0]));
            for (Map.Entry<Character, Material> e : craftIngredients.entrySet()) {
                recipe.setIngredient(e.getKey(), e.getValue());
            }
            Bukkit.addRecipe(recipe);
        } catch (IllegalArgumentException e) {
            log.warn("[slot_expander] craft invalide : {}", e.getMessage());
        }
    }

    public boolean isExpander(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return false;
        return stack.getItemMeta().getPersistentDataContainer().has(PDCBuilder.SLOT_EXPANDER, PersistentDataType.STRING);
    }

    public boolean isSupported(Material material) {
        return supported.contains(material);
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public NamespacedKey getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getAdded() {
        return added;
    }

    public int getMax() {
        return max;
    }

    public List<String> getDescription() {
        return description;
    }

    public List<String> getCraftShape() {
        return craftShape;
    }

    public Map<Character, Material> getCraftIngredients() {
        return craftIngredients;
    }

    private static Component mm(String text) {
        return MiniMessage.miniMessage().deserialize("<!italic>" + text);
    }
}
