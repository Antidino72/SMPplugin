package fr.antidino.smp.core.manager.perk;
import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.librairie.ItemBuilder;
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
import org.eclipse.sisu.inject.Logs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractPerk {

    private static final Logger log = LoggerFactory.getLogger(AbstractPerk.class);
    protected final String id;
    protected final String displayName;
    protected final CorePlugin plugin;
    protected final NamespacedKey key;
    protected final ItemStack item;

    public AbstractPerk(String id, String displayName, CorePlugin plugin, String keyName,FileConfiguration config) {
        this.id = id;
        this.displayName = displayName;
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, keyName);
        this.item = loadItem(config);
        registerCraft(config);

    }

    public String getId() {
        return id;
    }
    public ItemStack getItem() {
        return item;
    }
    public ItemStack loadItem(FileConfiguration configuration) {
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
        return new ItemBuilder(material,plugin).setName(MiniMessage.miniMessage().deserialize("<!italic>"+displayName)).build();
    }
    private ItemStack getNotFoundItem(){
        return new ItemBuilder(Material.BARRIER,plugin).setName(MiniMessage.miniMessage().deserialize("<red>NOT FOUND<red>")).build();
    }
    public String getDisplayName() {
        return displayName;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public void registerCraft(FileConfiguration config) {
        ItemStack item = loadItem(config);
        log.info("registering craft for item "+item.toString());
        NamespacedKey key = new NamespacedKey(plugin, "perk_" + config.getString("id", "custom"));

        ShapedRecipe recipe = new ShapedRecipe(key, item);

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        List<String> shape = config.getStringList("craft.shape");
        if (!shape.isEmpty()) {
            recipe.shape(shape.toArray(new String[0]));
        }


        ConfigurationSection ingredientsSec = config.getConfigurationSection("craft.ingredient");
        if (ingredientsSec != null) {
            for (String keyChar : ingredientsSec.getKeys(false)) {
                log.info(keyChar);
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
        log.info("registering craft for item "+recipe.getShape()[0]);
        log.info("registering craft for item "+recipe.getShape()[1]);
        log.info("registering craft for item "+recipe.getShape()[2]);
        if (Bukkit.getRecipe(key) == null) {
            Bukkit.addRecipe(recipe);
        }
    }

    public abstract ItemStack onApply(ItemStack item);
    public abstract void onRemove(Player player, ItemStack item);
}