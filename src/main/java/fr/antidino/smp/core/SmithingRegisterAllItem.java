package fr.antidino.smp.core;

import fr.antidino.smp.CorePlugin;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;

import java.util.ArrayList;
public class SmithingRegisterAllItem {
    public  SmithingRegisterAllItem(CorePlugin plugin) {
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
    public static ArrayList<Material> getAllMaterials(){
        ArrayList<Material> allArmorsList = new ArrayList<>();
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
