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
    public FileConfiguration SpeedConfig;

    public ConfigPerkManager(CorePlugin plugin) {

        this.plugin = plugin;
        configPerkLoader = new ConfigPerkLoader(plugin);
        LoadConfigAtStart();
    }
    public void LoadConfigAtStart(){

        plugin.saveResource("perks/health.yml", false);
        plugin.saveResource("perks/armor.yml", false);
        plugin.saveResource("perks/speed.yml", false);

        healthConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder()+"/perks/health.yml"));
        ArmorConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder()+"/perks/armor.yml"));
        SpeedConfig =  YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder()+"/perks/speed.yml"));


    }
    public void ReloadConfig(){
        healthConfig = YamlConfiguration.loadConfiguration(new File("perks/health.yml"));
        SpeedConfig = YamlConfiguration.loadConfiguration(new File("perks/speed.yml"));
        ArmorConfig = YamlConfiguration.loadConfiguration(new File("perks/armor.yml"));
    }
}
