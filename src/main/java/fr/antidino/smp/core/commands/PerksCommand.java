package fr.antidino.smp.core.commands;

import fr.antidino.smp.CorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/** /perks codex : ouvre le codex (liste des perks + crafts). */
public class PerksCommand implements CommandExecutor, TabCompleter {
    private final CorePlugin plugin;

    public PerksCommand(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.messageManager.get("player_only"));
            return true;
        }
        if (args.length >= 1 && args[0].equalsIgnoreCase("codex")) {
            plugin.codexMenu.openList(player, 0);
            return true;
        }
        player.sendMessage(plugin.messageManager.get("codex_usage"));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1 && "codex".startsWith(args[0].toLowerCase())) completions.add("codex");
        return completions;
    }
}
