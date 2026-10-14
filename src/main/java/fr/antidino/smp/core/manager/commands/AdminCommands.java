package fr.antidino.smp.core.manager.commands;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.manager.perk.AbstractPerk;
import fr.antidino.smp.core.manager.perk.PerkManager;
import fr.antidino.smp.core.manager.perk.type.HealthPerk;
import fr.antidino.smp.librairie.ItemBuilder;
import fr.antidino.smp.librairie.PDCBuilder;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

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
                player.sendMessage(Component.text("Config reloaded!"));
                break;
            }
            case "give" : {
                AbstractPerk perk = plugin.perkManager.getPerk(args[1]);
                if (perk == null) return false;

                int count = Integer.parseInt(args[2]);
                for (int i = 0; i < count; i++) {
                    player.getInventory().addItem(perk.getItem());
                }

                break;
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
            completions.add("armor_perk");
            completions.add("health_perk");
        }
        if (args.length == 3 && args[0].equals("give")) {
            completions.add("01");
            completions.add("02");
            completions.add("03");
            completions.add("04");
            completions.add("05");
            completions.add("10");
            completions.add("20");
            completions.add("30");
            completions.add("40");
            completions.add("50");
        }
        return completions;
    }
}
