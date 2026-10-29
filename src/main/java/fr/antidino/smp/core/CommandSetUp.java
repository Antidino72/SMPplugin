package fr.antidino.smp.core;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.modules.commands.AdminCommands;
import fr.antidino.smp.modules.commands.PerksCommand;

public class CommandSetUp {
    public static void registerCommands(CorePlugin plugin) {
        PerksCommand perksCommand = new PerksCommand(plugin);
        AdminCommands adminCommands = new AdminCommands(plugin);

        plugin.getCommand("perk").setExecutor(adminCommands);
        plugin.getCommand("perk").setTabCompleter(adminCommands);
        plugin.getCommand("perks").setExecutor(perksCommand);
        plugin.getCommand("perks").setTabCompleter(perksCommand);

    }
}
