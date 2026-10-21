package fr.antidino.smp.core.manager.perk;
import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.librairie.ItemBuilder;
import fr.antidino.smp.librairie.PDCBuilder;

import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractPerk {

    private static final Logger log = LoggerFactory.getLogger(AbstractPerk.class);
    protected final String id;
    protected final String displayName;
    protected final CorePlugin plugin;
    protected final NamespacedKey key;
    protected final ItemStack item;
    // Infos utilisées par le menu /perks codex
    protected final List<String> description;
    protected final List<String> craftShape;
    protected final Map<Character, Material> craftIngredients;
    // Niveaux + valeurs configurables
    protected final FileConfiguration config;
    protected final int maxLevel;

    private static final String[] ROMAN = {"0", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
    private static final int MAX_POSSIBLE_LEVEL = 10;

    public AbstractPerk(String id, CorePlugin plugin,FileConfiguration config) {
        this.id = id;
        this.displayName = loadName(config);
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, id);
        this.config = config;
        this.maxLevel = Math.clamp(config.getInt("level.max", defaultMaxLevel()), 1, MAX_POSSIBLE_LEVEL);
        this.item = loadItem(config);
        this.description = loadDescription(config);
        this.craftShape = loadCraftShape(config);
        this.craftIngredients = loadCraftIngredients(config);
        registerCraft(config);
        registerUpgradeRecipes();

    }

    /*
        Methode pour récupérer les infos d'un perk
     */
    public String getId() {
        return id;
    }
    public ItemStack getItem() {
        return item;
    }
    public String getDisplayName() {
        return displayName;
    }

    public NamespacedKey getKey() {
        return key;
    }

    /** Lignes de description (format MiniMessage) définies dans le fichier du perk. */
    public List<String> getDescription() {
        return description;
    }

    /** Forme du craft (max 3 lignes de 3 caractères). */
    public List<String> getCraftShape() {
        return craftShape;
    }

    /** Caractère de la forme -> matériau. */
    public Map<Character, Material> getCraftIngredients() {
        return craftIngredients;
    }

    // ------------------------------------------------------------ NIVEAUX

    /** Niveau max par défaut si "level.max" est absent du yml (surchargé par les perks à niveaux). */
    protected int defaultMaxLevel() {
        return 1;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    /** Nombre de perks du niveau N nécessaires (table de craft) pour obtenir le niveau N+1. */
    public int getUpgradeCost() {
        return Math.clamp(config.getInt("level.upgrade_cost", 2), 2, 9);
    }

    /** Multiplicateur d'effet : 1 au niveau 1, puis + "level.bonus_per_level" par niveau (0.5 par défaut). */
    public double getMultiplier(int level) {
        double bonus = config.getDouble("level.bonus_per_level", 0.5);
        return 1.0 + bonus * (Math.max(1, level) - 1);
    }
    /*
        Get Number of craft in world
     */
    public int getMaxPossibleCraft() {
        return config.getInt("craft.number");

    }
    /**
     * Valeur configurable par niveau dans la section "values" du yml.
     * Liste : [niv1, niv2, niv3] (le dernier est réutilisé au-delà). Nombre seul : même valeur partout.
     */
    public double getValue(String name, int level, double def) {
        String path = "values." + name;
        if (config.isList(path)) {
            List<Double> list = config.getDoubleList(path);
            if (!list.isEmpty()) {
                return list.get(Math.max(0, Math.min(level, list.size()) - 1));
            }
        } else if (config.isSet(path)) {
            return config.getDouble(path, def);
        }
        return def;
    }

    /** Item du perk au niveau demandé (niveau 1 = item d'origine, inchangé). */
    public ItemStack getItem(int level) {
        int lvl = Math.max(1, Math.min(level, maxLevel));
        ItemStack copy = item.clone();
        if (lvl == 1) return copy;
        ItemMeta meta = copy.getItemMeta();
        if (meta == null) return copy;
        meta.getPersistentDataContainer().set(PDCBuilder.PERK_LEVEL, PersistentDataType.INTEGER, lvl);
        meta.displayName(MiniMessage.miniMessage().deserialize("<!italic>" + displayName + " <gray>" + roman(lvl)));
        meta.lore(List.of(MiniMessage.miniMessage().deserialize("<!italic><gray>Niveau <white>" + roman(lvl))));
        copy.setItemMeta(meta);
        return copy;
    }

    public static String roman(int n) {
        return n >= 1 && n < ROMAN.length ? ROMAN[n] : String.valueOf(n);
    }

    private NamespacedKey upgradeKey(int level) {
        return new NamespacedKey(plugin, id + "_upgrade_" + level);
    }

    private void registerUpgradeRecipes() {
        for (int lvl = 2; lvl <= MAX_POSSIBLE_LEVEL; lvl++) {
            Bukkit.removeRecipe(upgradeKey(lvl));
        }
        if (maxLevel <= 1) return;
        int cost = getUpgradeCost();
        for (int lvl = 1; lvl < maxLevel; lvl++) {
            ShapelessRecipe recipe = new ShapelessRecipe(upgradeKey(lvl + 1), getItem(lvl + 1));
            RecipeChoice.ExactChoice choice = RecipeChoice.exactChoice(getItem(lvl));
            for (int i = 0; i < cost; i++) {
                recipe.addIngredient(choice);
            }
            Bukkit.addRecipe(recipe);
        }
    }

    /** Retire le craft du perk et ses crafts d'amélioration (utilisé par /perk reload). */
    public void unregisterRecipes() {
        Bukkit.removeRecipe(key);
        for (int lvl = 2; lvl <= MAX_POSSIBLE_LEVEL; lvl++) {
            Bukkit.removeRecipe(upgradeKey(lvl));
        }
    }
    /*
            Fonction utilitaire pour Charger les infos
     */

    private String loadName(FileConfiguration config) {
        return config.getString("item.display_name","<red>Display name");
    }
    private List<String> loadDescription(FileConfiguration config) {
        return List.copyOf(config.getStringList("description"));
    }
    private List<String> loadCraftShape(FileConfiguration config) {
        return List.copyOf(config.getStringList("craft.shape"));
    }
    private Map<Character, Material> loadCraftIngredients(FileConfiguration config) {
        Map<Character, Material> map = new LinkedHashMap<>();
        ConfigurationSection sec = config.getConfigurationSection("craft.ingredient");
        if (sec == null) return Collections.unmodifiableMap(map);
        for (String k : sec.getKeys(false)) {
            if (k.isEmpty()) continue;
            String name = sec.getString(k);
            if (name == null) continue;
            Material m = Material.matchMaterial(name);
            if (m != null) map.put(k.charAt(0), m);
        }
        return Collections.unmodifiableMap(map);
    }
    private ItemStack loadItem(FileConfiguration configuration) {
        if (configuration == null) {
            return getNotFoundItem();
        }
        String displayName = configuration.getString("item.display_name");
        if (displayName == null) {
            log.info("displayName config not found");
            return getNotFoundItem();
        }
        String material_string = configuration.getString("item.material");
        if (material_string == null) {
            log.info("material " + null);
            return getNotFoundItem();
        }
        Material material = Material.getMaterial(material_string);

        if(material == null){
            plugin.getLogger().warning(configuration.getString("item.material")+ " not found");
            return getNotFoundItem();
        }
        return new ItemBuilder(material,plugin).addPDC(PDCBuilder.PERK_ID,getId()).setName(MiniMessage.miniMessage().deserialize("<!italic>"+displayName)).build();
    }
    private ItemStack getNotFoundItem(){
        return new ItemBuilder(Material.BARRIER,plugin).setName(MiniMessage.miniMessage().deserialize("<red>NOT FOUND<red>")).build();
    }
    private void registerCraft(FileConfiguration config) {
        if (Bukkit.getRecipe(key) != null) {
            Bukkit.removeRecipe(key);
        }
        ItemStack item = loadItem(config);


        ShapedRecipe recipe = new ShapedRecipe(key, item);

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        List<String> shape = config.getStringList("craft.shape");
        if (!shape.isEmpty()) {
            recipe.shape(shape.toArray(new String[0]));
        }


        ConfigurationSection ingredientsSec = config.getConfigurationSection("craft.ingredient");
        if (ingredientsSec != null) {
            for (String keyChar : ingredientsSec.getKeys(false)) {
                if (keyChar.isEmpty()) continue;
                char c = keyChar.charAt(0);

                String materialName = ingredientsSec.getString(keyChar);
                if (materialName != null) {
                    Material material = Material.matchMaterial(materialName);
                    if (material != null) {
                        recipe.setIngredient(c, material);
                    }else {
                        log.info("Material {} not found", materialName);
                    }
                }else {
                    log.info("MaterialName {} not found", keyChar);
                }
            }
        }else {
            log.info("craft ingredient section is empty");
        }
        if (Bukkit.getRecipe(key) == null) {
            Bukkit.addRecipe(recipe);
        }
    }

    /*
        Fonctions pour gérer les evenement des perk
     */

    public abstract ItemStack onApply(ItemStack item);

    /** Application avec niveau. Par défaut, ignore le niveau (perks sans effet à échelle). */
    public ItemStack onApply(ItemStack item, int level) {
        return onApply(item);
    }
    public abstract void onRemove(Player player, ItemStack item);



}