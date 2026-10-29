package fr.antidino.smp.modules.perk;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.modules.perk.config.ConfigPerkManager;
import fr.antidino.smp.modules.perk.config.PerkTypeId;
import fr.antidino.smp.modules.perk.type.*;
import org.bukkit.inventory.EquipmentSlot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerkRegistry {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    public Map<String, AbstractPerk> registeredPerks = new HashMap<>();
    private final CorePlugin plugin;
    private final PerkManager perkManager;

    public PerkRegistry(CorePlugin plugin,PerkManager perkManager) {
        this.plugin = plugin;
        this.perkManager = perkManager;
        registerDefaults();

    }

    private void registerDefaults() {

        ConfigPerkManager configPerkManager = perkManager.configPerkManager;

        registerPerk(new NoFallPerk(PerkTypeId.NO_FALL_PERK,plugin,configPerkManager.NoFallConfig));
        registerPerk(new WardenPerk(PerkTypeId.WARDEN_PERK,plugin,configPerkManager.WardenConfig));

        // Nouveaux perks (effets gérés par PerkTasks / PerkEffectListener)
        registerPerk(new PassivePerk(PerkTypeId.DOUBLE_JUMP, plugin, configPerkManager.get("double_jump.yml")));
        registerPerk(new PassivePerk(PerkTypeId.NIGHT_VISION, plugin, configPerkManager.get("night_vision.yml")));
        registerPerk(new PassivePerk(PerkTypeId.MAGNET, plugin, configPerkManager.get("magnet.yml")));
        registerPerk(new PassivePerk(PerkTypeId.SMELTER, plugin, configPerkManager.get("smelter.yml")));
        registerPerk(new PassivePerk(PerkTypeId.LIFESTEAL_PERK,plugin,configPerkManager.LifeStealConfig));

        // Perks pilotés par une liste de modificateurs d'attributs dans le yml (bonus + malus)
        registerPerk(new AttributePerk(PerkTypeId.SPEED,plugin,configPerkManager.SpeedConfig));
        registerPerk(new AttributePerk(PerkTypeId.ATTACK_SPEED,plugin,configPerkManager.AttackSpeedConfig));
        registerPerk(new AttributePerk(PerkTypeId.ATTACK_DAMAGE,plugin,configPerkManager.AttackDamageConfig));
        registerPerk(new AttributePerk(PerkTypeId.ARMOR,plugin,configPerkManager.get("armor.yml")));
        registerPerk(new AttributePerk(PerkTypeId.HEALTH,plugin,configPerkManager.healthConfig));
        registerPerk(new AttributePerk(PerkTypeId.MINER, plugin, configPerkManager.get("miner.yml")));
        registerPerk(new AttributePerk(PerkTypeId.SPRINTER, plugin, configPerkManager.get("sprinter.yml")));
        registerPerk(new AttributePerk(PerkTypeId.GLASS_CANNON, plugin, configPerkManager.get("glass_cannon.yml")));
    }

    /** Recharge tous les perks depuis les yml (crafts et crafts d'amélioration inclus). */
    public void reload() {
        registeredPerks.values().forEach(AbstractPerk::unregisterRecipes);
        registeredPerks.clear();
        registerDefaults();

    }

    public void registerPerk(AbstractPerk perk) {
        registeredPerks.put(perk.getId(), perk);
    }






    public List<String> listPerks(){
        return new ArrayList<>(registeredPerks.keySet());
    }
}
