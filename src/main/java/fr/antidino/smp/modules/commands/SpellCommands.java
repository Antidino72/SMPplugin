package fr.antidino.smp.modules.commands;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.MessageManager;
import fr.antidino.smp.modules.spell.AbstractSpell;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SpellCommands implements CommandExecutor , TabCompleter {
    private final CorePlugin plugin;
    private final MessageManager messageManager;
    public SpellCommands(CorePlugin plugin) {
        this.plugin = plugin;
        messageManager = plugin.messageManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("smp.spell")) {
            sender.sendMessage(messageManager.get("insufficient_permission"));
            return true;
        }
        if (!(sender instanceof Player player)){
            sender.sendMessage(messageManager.get("player_only"));
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(messageManager.get("spell_command_usage"));
            return false;
        }

        String spell_id = args[0];
        AbstractSpell spell = plugin.spellManager.getSpell(spell_id);
        if (spell == null) {
            sender.sendMessage(messageManager.get("spell_not_found"));
            return false;
        }
        if (spell.canDoSpell(player)) {
            sender.sendMessage(messageManager.get("insufficient_permission"));
        }
        spell.cast(player);
        spell.PerformCooldown(player);

        return true;
    }
    @Override
    public ArrayList<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        ArrayList<String> completions = new ArrayList<>();
        plugin.spellManager.getSpells().forEach(spell -> {
            completions.add(spell.getId());
        });
        return completions;
    }
}
