package fr.antidino.smp.modules.perk.cache;

import fr.antidino.smp.CorePlugin;
import org.bukkit.Bukkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DataCache {
    private static final Logger log = LoggerFactory.getLogger(DataCache.class);
    private final HashMap<String,Integer> perk_craft_count = new HashMap<>();
    private final CorePlugin plugin;
    public DataCache(CorePlugin plugin) {
        this.plugin = plugin;
    }
    public void load() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (String id : plugin.perkManager.listPerks()){
                int numberOfCraft = plugin.databaseProvider.getNumberOfCraft(id);
                perk_craft_count.put(id, numberOfCraft);
            }
            log.info("Loaded {} perks", perk_craft_count.size());
        });
    }
    public void unload() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (String id : plugin.perkManager.listPerks()){
                perk_craft_count.remove(id);
            }
        });
    }
    public int getPerkCraftCount(String id) {
        return perk_craft_count.getOrDefault(id,0);
    }
    public void addPerkCraftCount(String id) {
        int numberOfCraft = getPerkCraftCount(id);
        perk_craft_count.put(id,numberOfCraft+1);
    }


    /**Debug **/
    public Map<String, Integer> getPerkCraftCount() {
        return perk_craft_count;
    }
}
