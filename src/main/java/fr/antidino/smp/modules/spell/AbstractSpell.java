package fr.antidino.smp.modules.spell;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.CooldownManager;

import fr.antidino.smp.core.PermissionsManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

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
    protected Permission permission;
    protected int cooldown;

    public AbstractSpell(String id, FileConfiguration spellConfig,CorePlugin plugin) {
        this.id = id;
        this.cooldownManager = new CooldownManager();
        this.spellConfig = spellConfig;
        this.plugin = plugin;

        loadConfigValues();
    }


    private void loadConfigValues() {
        this.permission = PermissionsManager.RegisterPermission(spellConfig);
        this.spellName = spellConfig.getString("name");
        this.spellDamageMin = (float) spellConfig.getDouble("damage.min", 2.0);
        this.spellDamageMax = (float) spellConfig.getDouble("damage.max", 10.0);
        this.spellLore = spellConfig.getStringList("lore");
        this.cooldown = spellConfig.getInt("cooldown");
    }


    public boolean canDoSpell(Player player) {
        if (player.hasPermission(this.permission)) {
            return true;
        }
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