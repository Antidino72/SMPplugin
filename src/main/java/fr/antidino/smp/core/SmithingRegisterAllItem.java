package fr.antidino.smp.core;

import fr.antidino.smp.CorePlugin;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SmithingRegisterAllItem {
    public static void RegisterAllItem(CorePlugin plugin) {
        NamespacedKey key = new NamespacedKey(plugin, "custom_all_armor_smithing");

        ArrayList<Material> items = new ArrayList<>();
        items.add(Material.ECHO_SHARD);


        RecipeChoice base = new RecipeChoice.MaterialChoice(getAllMaterials());

        plugin.perkManager.getPerks().forEach(perk -> items.add(perk.getItem().getType()));

        RecipeChoice addition = new RecipeChoice.MaterialChoice(items.stream().distinct().toList());



        ItemStack result = new ItemStack(Material.STONE);
        SmithingRecipe recipe = new SmithingTransformRecipe(key, result, addition, base, addition);
        plugin.getServer().addRecipe(recipe);
    }
    public static Map<String, List<Material>> getCategorizedMaterials() {
        Map<String, List<Material>> categories = new LinkedHashMap<>();

        List<Material> armors = new ArrayList<>();
        List<Material> toolsAndWeapons = new ArrayList<>();

        for (Material mat : Material.values()) {
            if (!mat.isItem()) continue;

            String name = mat.name();


            if (name.endsWith("_HELMET") ||
                    name.endsWith("_CHESTPLATE") ||
                    name.endsWith("_LEGGINGS") ||
                    name.endsWith("_BOOTS")) {
                armors.add(mat);
            }

            else if (name.endsWith("_SWORD") ||
                    name.endsWith("_AXE") ||
                    name.endsWith("_PICKAXE") ||
                    name.endsWith("_SHOVEL") ||
                    name.endsWith("SHIELD")) {
                toolsAndWeapons.add(mat);
            }
        }


        categories.put("Armures", armors);
        categories.put("Outils & Armes", toolsAndWeapons);

        return categories;
    }


    public static List<Material> getAllMaterials() {
        List<Material> allItems = new ArrayList<>();
        getCategorizedMaterials().values().forEach(allItems::addAll);
        return allItems;
    }
}

