package fr.antidino.smp.core.manager.perk.config;

import fr.antidino.smp.CorePlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigPerkManager {
    CorePlugin plugin;
    public ConfigPerkLoader configPerkLoader;

    public FileConfiguration healthConfig;
    public FileConfiguration ArmorConfig;
    public FileConfiguration SpeedConfig;
    public FileConfiguration AttackSpeedConfig;
    public FileConfiguration AttackDamageConfig;

    public ConfigPerkManager(CorePlugin plugin) {

        this.plugin = plugin;
        configPerkLoader = new ConfigPerkLoader(plugin);
        LoadConfigAtStart();
    }
    public void LoadConfigAtStart(){

        List<String> immutableList = List.of("health.yml", "attack_damage.yml", "speed.yml", "armor.yml","attack_speed.yml");
        immutableList.forEach(this::generateFile);
        ReloadConfig();


    }
    public void ReloadConfig(){
        healthConfig = YamlConfiguration.loadConfiguration(getConfigFile("health.yml"));
        AttackDamageConfig = YamlConfiguration.loadConfiguration(getConfigFile("attack_damage.yml"));
        SpeedConfig = YamlConfiguration.loadConfiguration(getConfigFile("speed.yml"));
        AttackSpeedConfig = YamlConfiguration.loadConfiguration(getConfigFile("attack_speed.yml"));
        ArmorConfig = YamlConfiguration.loadConfiguration(getConfigFile("armor.yml"));
    }
    private File getConfigFile(String name){
        return new File(plugin.getDataFolder()+"/perks/"+name);
    }
    private void generateFile(String name){
        plugin.saveResource("perks/"+name, false);
    }
}
