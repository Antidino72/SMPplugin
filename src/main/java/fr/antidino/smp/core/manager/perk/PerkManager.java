package fr.antidino.smp.core.manager.perk;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.manager.perk.config.ConfigPerkLoader;
import fr.antidino.smp.core.manager.perk.config.ConfigPerkManager;
import fr.antidino.smp.core.manager.perk.type.*;
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
        registerPerk(new HealthPerk("health_perk",plugin,configPerkManager.healthConfig));
        registerPerk(new ArmorPerk("armor_perk",plugin,configPerkManager.ArmorConfig));
        registerPerk(new SpeedPerk("speed_perk",plugin,configPerkManager.SpeedConfig));
        registerPerk(new AttackSpeedPerk("attack_speed_perk",plugin,configPerkManager.AttackSpeedConfig));
        registerPerk(new AttackDamagePerk("attack_damage_perk",plugin,configPerkManager.AttackDamageConfig));
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