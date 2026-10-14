package fr.antidino.smp.core;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class SmithingRegisterAllItem {
    public  SmithingRegisterAllItem(Plugin plugin) {
        NamespacedKey key = new NamespacedKey(plugin, "custom_all_armor_smithing");



        RecipeChoice base = new RecipeChoice.MaterialChoice(getAllMaterials());

        RecipeChoice addition = new RecipeChoice.MaterialChoice(Material.BARRIER);



        ItemStack result = new ItemStack(Material.STONE);
        SmithingRecipe recipe = new SmithingTransformRecipe(key, result, addition, base, addition);
        plugin.getServer().addRecipe(recipe);
    }
    public static List<Material> getAllMaterials(){
        List<Material> allArmorsList = new ArrayList<>();
        for (Material mat : Material.values()) {
            if (mat.isItem()) {
                String name = mat.name();
                if (name.endsWith("_HELMET") ||
                        name.endsWith("_CHESTPLATE") ||
                        name.endsWith("_LEGGINGS") ||
                        name.endsWith("_BOOTS") ||
                        name.endsWith("_SWORD") ||
                        name.endsWith("_AXE") ||
                        name.endsWith("_PICKAXE") ||
                        name.endsWith("_SHOVEL")  ||
                        name.endsWith("SHIELD")
                ) {


                    allArmorsList.add(mat);
                }
            }
        }
        return allArmorsList;
    }
}
