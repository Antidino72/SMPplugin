package fr.antidino.smp.modules.spell;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.modules.spell.type.FlameSpell;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class SpellManager {
    private final Map<String, AbstractSpell> registeredSpells = new HashMap<>();
    private final CorePlugin plugin;
    private final SpellConfig spellConfig;


    public SpellManager(CorePlugin plugin) {
        this.plugin = plugin;
        this.spellConfig = new SpellConfig(plugin);
        registerDefaults();
    }

    private void registerDefaults() {
            registerSpell(new FlameSpell(SpellTypeID.flame,spellConfig.FlameConfiguration,plugin));
    }

    /** Recharge tous les perks depuis les yml (crafts et crafts d'amélioration inclus). */
    public void reload() {
        registeredSpells.clear();
        registerDefaults();

    }

    public void registerSpell(AbstractSpell spell) {
        registeredSpells.put(spell.getId(), spell);
    }


    public AbstractSpell getSpell(String id) {
        return registeredSpells.get(id);
    }
    public ArrayList<AbstractSpell> getSpells() {
        return new ArrayList<>(registeredSpells.values());
    }
   
    
}
