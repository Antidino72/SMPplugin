package fr.antidino.smp.core.manager.perk;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.manager.perk.config.ConfigPerkLoader;
import fr.antidino.smp.core.manager.perk.config.ConfigPerkManager;
import fr.antidino.smp.core.manager.perk.type.ArmorPerk;
import fr.antidino.smp.core.manager.perk.type.HealthPerk;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PerkManager {
    private final Map<String, AbstractPerk> registeredPerks = new HashMap<>();
    private final CorePlugin plugin;


    public PerkManager(CorePlugin plugin) {
        this.plugin = plugin;
        registerDefaults();
    }

    private void registerDefaults() {

        ConfigPerkManager configPerkManager = plugin.configPerkManager;
        ConfigPerkLoader configPerkLoader = configPerkManager.configPerkLoader;
        registerPerk(new HealthPerk("health_perk", "Cœur Supplémentaire",plugin,configPerkManager.healthConfig));
        registerPerk(new ArmorPerk("armor_perk","Résistance Supplémentaire",plugin,configPerkManager.ArmorConfig));
    }

    public void registerPerk(AbstractPerk perk) {
        registeredPerks.put(perk.getId(), perk);
    }

    public AbstractPerk getPerk(String id) {
        return registeredPerks.get(id);
    }
    public ArrayList<AbstractPerk> getPerks() {
        return new ArrayList<>(registeredPerks.values());
    }
}