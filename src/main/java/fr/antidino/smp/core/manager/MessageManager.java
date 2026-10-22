package fr.antidino.smp.core.manager;

import fr.antidino.smp.CorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private static final Logger log = LoggerFactory.getLogger(MessageManager.class);
    private final CorePlugin plugin;
    private final Map<String, String> cache = new HashMap<>();

    public MessageManager(CorePlugin plugin) {
        this.plugin = plugin;
        reloadConfig();
    }
    public void reloadConfig() {
        loadMessages(plugin.getConfig().getString("lang","fr_FR"));
    }
    public void loadMessages(String langCode) {
        cache.clear();
        File langFile = new File(plugin.getDataFolder(), "lang/" + langCode + ".yml");


        if (!langFile.exists()) {
            plugin.saveResource("lang/" + langCode + ".yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(langFile);


        for (String key : config.getKeys(true)) {
            if (!config.isConfigurationSection(key)) {
                String value = config.getString(key, "");
                cache.put(key, value);
            }
        }
        log.info("Loaded " + cache.size() + " messages.");
    }


    public Component get(String key, Map<String, String> replacements) {
        String message = cache.getOrDefault(key, "<red>Message introuvable: " + key);

        if (replacements != null) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                message = message.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }

        return MiniMessage.miniMessage().deserialize(message);
    }


    public Component get(String key) {
        return get(key, null);
    }
}