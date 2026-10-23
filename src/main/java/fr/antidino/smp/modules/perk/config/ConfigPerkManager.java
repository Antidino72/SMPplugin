package fr.antidino.smp.modules.perk.config;

import fr.antidino.smp.CorePlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigPerkManager {
    final CorePlugin plugin;

    /** Tous les fichiers de perks (dans resources/perks/ et plugins/SMPplugin/perks/). */
    public static final List<String> FILES = List.of(
            "health.yml", "attack_damage.yml", "speed.yml", "armor.yml", "attack_speed.yml", "nofall.yml", "warden.yml",
            "double_jump.yml", "night_vision.yml", "magnet.yml", "smelter.yml",
            "miner.yml", "sprinter.yml", "glass_cannon.yml"
    );

    private final Map<String, FileConfiguration> configs = new HashMap<>();

    public FileConfiguration healthConfig;
    public FileConfiguration ArmorConfig;
    public FileConfiguration SpeedConfig;
    public FileConfiguration AttackSpeedConfig;
    public FileConfiguration AttackDamageConfig;
    public FileConfiguration NoFallConfig;
    public FileConfiguration WardenConfig;
    public FileConfiguration LifeStealConfig;

    public ConfigPerkManager(CorePlugin plugin) {

        this.plugin = plugin;
        LoadConfigAtStart();
    }
    public void LoadConfigAtStart(){

        FILES.forEach(this::generateFile);
        List<String> immutableList = List.of(
                "health.yml", "attack_damage.yml",
                "speed.yml", "armor.yml",
                "attack_speed.yml","nofall.yml",
                "warden.yml","lifesteal.yml");
        immutableList.forEach(this::generateFile);
        ReloadConfig();


    }
    public void ReloadConfig(){
        configs.clear();
        for (String name : FILES) {
            configs.put(name, YamlConfiguration.loadConfiguration(getConfigFile(name)));
        }
        healthConfig = configs.get("health.yml");
        AttackDamageConfig = configs.get("attack_damage.yml");
        SpeedConfig = configs.get("speed.yml");
        AttackSpeedConfig = configs.get("attack_speed.yml");
        ArmorConfig = configs.get("armor.yml");
        NoFallConfig = configs.get("nofall.yml");
        WardenConfig = configs.get("warden.yml");
        healthConfig = YamlConfiguration.loadConfiguration(getConfigFile("health.yml"));
        AttackDamageConfig = YamlConfiguration.loadConfiguration(getConfigFile("attack_damage.yml"));
        SpeedConfig = YamlConfiguration.loadConfiguration(getConfigFile("speed.yml"));
        AttackSpeedConfig = YamlConfiguration.loadConfiguration(getConfigFile("attack_speed.yml"));
        ArmorConfig = YamlConfiguration.loadConfiguration(getConfigFile("armor.yml"));
        NoFallConfig = YamlConfiguration.loadConfiguration(getConfigFile("nofall.yml"));
        WardenConfig = YamlConfiguration.loadConfiguration(getConfigFile("warden.yml"));
        LifeStealConfig = YamlConfiguration.loadConfiguration(getConfigFile("lifesteal.yml"));

    }

    /** Récupère la config d'un perk par nom de fichier (ex: "vampire.yml"). */
    public FileConfiguration get(String fileName) {
        return configs.get(fileName);
    }
    private File getConfigFile(String name){

        return new File(plugin.getDataFolder()+"/perks/"+name);
    }
    private void generateFile(String name){
        File file = new File(plugin.getDataFolder()+"/perks/"+name);
        if(!file.exists()){
            plugin.saveResource("perks/"+name, false);

        }
    }
}
