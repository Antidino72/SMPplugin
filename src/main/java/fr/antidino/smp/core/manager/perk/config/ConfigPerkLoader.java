package fr.antidino.smp.core.manager.perk.config;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.librairie.ItemBuilder;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigPerkLoader {
    private static final Logger log = LoggerFactory.getLogger(ConfigPerkLoader.class);
    CorePlugin plugin;
    public ConfigPerkLoader(CorePlugin plugin) {
        this.plugin = plugin;
    }
    public ItemStack getItem(FileConfiguration configuration) {
        String displayName = plugin.configPerkManager.healthConfig.getString("item.display_name");
        Material material = Material.getMaterial(plugin.configPerkManager.healthConfig.getString("item.material"));
        if(material == null){
            log.warn(plugin.configPerkManager.healthConfig.getString("item.material")+ " not found");
            return getNotFoundItem();
        }
        if(displayName == null){
            log.warn(plugin.configPerkManager.healthConfig.getString("item.display_name"));
            return getNotFoundItem();
        }
        return new ItemBuilder(material,plugin).setName(MiniMessage.miniMessage().deserialize(displayName)).build();
    }
    private ItemStack getNotFoundItem(){
        return new ItemBuilder(Material.BARRIER,plugin).setName(MiniMessage.miniMessage().deserialize("<red>NOT FOUND<red>")).build();
    }

}
