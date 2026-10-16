package fr.antidino.smp.core.manager.perk.config;

import fr.antidino.smp.CorePlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigPerkManager {
    CorePlugin plugin;
    public ConfigPerkLoader configPerkLoader;

    public FileConfiguration healthConfig;
    public FileConfiguration ArmorConfig;

    public ConfigPerkManager(CorePlugin plugin) {

        this.plugin = plugin;
        configPerkLoader = new ConfigPerkLoader(plugin);
        LoadConfigAtStart();
    }
    public void LoadConfigAtStart(){

        plugin.saveResource("perks/health.yml", false);
        plugin.saveResource("perks/armor.yml", false);

        healthConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder()+"/perks/health.yml"));
        ArmorConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder()+"/perks/armor.yml"));

    }
    public void ReloadConfig(){
        healthConfig = YamlConfiguration.loadConfiguration(new File("perks/health.yml"));
        ArmorConfig = YamlConfiguration.loadConfiguration(new File("perks/armor.yml"));
    }
}
