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
import org.bukkit.inventory.ShapedRecipe;
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

    public AbstractPerk(String id, CorePlugin plugin,FileConfiguration config) {
        this.id = id;
        this.displayName = loadName(config);
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, id);
        this.item = loadItem(config);
        this.description = loadDescription(config);
        this.craftShape = loadCraftShape(config);
        this.craftIngredients = loadCraftIngredients(config);
        registerCraft(config);

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
    /*
            Fonction utilitaire pour Charger les infos
     */

    private String loadName(FileConfiguration config) {
        return config.getString("item.display_name","<red>Display name");
    }
    private List<String> loadDescription(FileConfiguration config) {
        return Collections.unmodifiableList(new ArrayList<>(config.getStringList("description")));
    }
    private List<String> loadCraftShape(FileConfiguration config) {
        return Collections.unmodifiableList(new ArrayList<>(config.getStringList("craft.shape")));
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
            Bukkit.getLogger().warning(configuration.getString("item.material")+ " not found");
            return getNotFoundItem();
        }
        if(displayName == null){
            Bukkit.getLogger().warning(configuration.getString("item.display_name"));
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
                        log.info("Material " + materialName + " not found");
                    }
                }else {
                    log.info("MaterialName " + keyChar + " not found");
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
    public abstract void onRemove(Player player, ItemStack item);



}