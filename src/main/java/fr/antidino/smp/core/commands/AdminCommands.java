package fr.antidino.smp.core.commands;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.manager.perk.AbstractPerk;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AdminCommands implements CommandExecutor, TabCompleter {
    private CorePlugin plugin;
    public AdminCommands(CorePlugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) return true;
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        switch (args[0]) {
            case "reload": {
                plugin.reloadConfig();
                plugin.initConfig();
                player.sendMessage(Component.text("Config reloaded!"));
                break;
            }
            case "give" : {
                AbstractPerk perk = plugin.perkManager.getPerk(args[1]);
                if (perk == null) return false;
                if (args.length == 2) {
                    player.getInventory().addItem(perk.getItem());
                    break;
                }else if (args.length == 3) {
                    int count = Integer.parseInt(args[2]);

                    for (int i = 0; i < count; i++) {
                        player.getInventory().addItem(perk.getItem());
                    }
                    break;
                }
            }
        }
        return true;
    }
    @Override
    public ArrayList<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        ArrayList<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("reload");
            completions.add("give");
        }
        if (args.length == 2 &&  args[0].equals("give")) {
            plugin.perkManager.getPerks().forEach(perk -> {
                completions.add(perk.getId());
            });
        }
        if (args.length == 3 && args[0].equals("give")) {
            completions.addAll(List.of("01", "02", "04","08","16","32","64"));
        }
        return completions;
    }
}
