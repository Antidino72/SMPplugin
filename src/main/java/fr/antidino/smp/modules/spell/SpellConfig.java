package fr.antidino.smp.modules.spell;

import fr.antidino.smp.CorePlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpellConfig {
    final CorePlugin plugin;
    public FileConfiguration FlameConfiguration;


    /** Tous les fichiers de perks (dans resources/perks/ et plugins/SMPplugin/perks/). */
    public static final List<String> FILES =  List.of("flame.yml");

    private final Map<String, FileConfiguration> configs = new HashMap<>();

    public SpellConfig(CorePlugin plugin) {

        this.plugin = plugin;
        LoadConfigAtStart();
    }
    public void LoadConfigAtStart(){

        FILES.forEach(this::generateFile);
        ReloadConfig();


    }
    public void ReloadConfig(){
        configs.clear();
        for (String name : FILES) {
            configs.put(name, YamlConfiguration.loadConfiguration(getConfigFile(name)));
        }
        FlameConfiguration = configs.get("flame.yml");


    }

    /** Récupère la config d'un perk par nom de fichier (ex: "vampire.yml"). */
    public FileConfiguration get(String fileName) {
        return configs.get(fileName);
    }
    private File getConfigFile(String name){

        return new File(plugin.getDataFolder()+"/spells/"+name);
    }
    private void generateFile(String name){
        File file = new File(plugin.getDataFolder()+"/spells/"+name);
        if(!file.exists()){
            plugin.saveResource("spells/"+name, false);

        }
    }
}
