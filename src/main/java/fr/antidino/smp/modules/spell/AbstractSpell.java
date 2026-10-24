package fr.antidino.smp.modules.spell;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.CooldownManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import java.util.List;

public abstract class AbstractSpell {
    protected String id;
    protected String spellName;
    protected FileConfiguration spellConfig;
    protected float spellDamage;
    protected float spellDamageMax;
    protected float spellDamageMin;
    protected List<String> spellLore;
    protected CooldownManager cooldownManager;
    protected CorePlugin plugin;
    protected int cooldown;

    public AbstractSpell(String id ,String spellName, FileConfiguration spellConfig,CorePlugin plugin) {
        this.spellName = spellName;
        this.id = id;
        this.cooldownManager = new CooldownManager();
        this.spellConfig = spellConfig;
        this.plugin = plugin;

        loadConfigValues();
    }


    public void loadConfigValues() {
        if (spellConfig != null && spellConfig.contains("spells." + spellName)) {
            this.spellDamage = (float) spellConfig.getDouble("spells." + spellName + ".damage", 5.0);
            this.spellDamageMin = (float) spellConfig.getDouble("spells." + spellName + ".damage-min", 2.0);
            this.spellDamageMax = (float) spellConfig.getDouble("spells." + spellName + ".damage-max", 10.0);
            this.spellLore = spellConfig.getStringList("spells." + spellName + ".lore");
            this.cooldown = spellConfig.getInt("spells." + spellName + ".cooldown");
        }
    }
    public boolean canDoSpell(Player player) {
        return cooldownManager.getRemainingSeconds(player.getUniqueId()) <= 0;
    }
    public void PerformCooldown(Player player) {
        cooldownManager.setCooldown(player.getUniqueId(), cooldown);
    }

    public abstract void cast(Player player);

    public String getId() {
        return id;
    }
    public String getSpellName() { return spellName; }
    public float getSpellDamage() { return spellDamage; }
    public List<String> getSpellLore() { return spellLore; }
}