package fr.antidino.smp.core.commands;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.SmithingRegisterAllItem;
import fr.antidino.smp.core.manager.perk.AbstractPerk;
import fr.antidino.smp.librairie.ChatUtil;
import fr.antidino.smp.librairie.PDCBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * /perk reload                              : recharge config, perks et crafts
 * /perk give <perk|slot_expander> [n] [niv] : donne un perk (niveau optionnel) ou l'item d'extension de slots
 */
public class AdminCommands implements CommandExecutor, TabCompleter {
    private static final String EXPANDER_ID = "slot_expander";
    private final CorePlugin plugin;
    public AdminCommands(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            send(sender, "<gray>Usage : <yellow>/perk reload <gray>| <yellow>/perk give <perk|slot_expander> [nombre] [niveau]");
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "slot" -> slot(sender, args);

            case "reload" -> {
                int count = plugin.fullReload();
                send(sender, "<green>Config rechargée : <white>" + count + " <green>perks, crafts et item de slots mis à jour.");
            }
            case "give" -> give(sender, args);
            default -> send(sender, "<red>Sous-commande inconnue. <gray>Essaie <yellow>/perk reload <gray>ou <yellow>/perk give");
        }
        return true;
    }
    private void slot(CommandSender sender, String[] args) {
        if (args.length < 2) {
            send(sender, "<yellow><b>Usage : <b/>/perk slot [argument]");
            return;
        }
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "init" -> {
                if (!(sender instanceof Player player)) {
                    send(sender, "<red>Only players can execute this command.");
                    break;
                }
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() == Material.AIR) {
                    send(sender, "<red>You must have an item in your hand!");
                    break;
                }
                if (!SmithingRegisterAllItem.getAllMaterials().contains(item.getType())) {
                    Component message = ChatUtil.createHoverableMaterialList();
                    sender.sendMessage(message);
                    break;
                }
                ItemStack updatedItem = plugin.pdcBuilder.initializeSlots(item);
                player.getInventory().setItemInMainHand(updatedItem);
                send(sender, "<green>Item successfully initialized!");
            }
            case "apply" -> {
                if (!(sender instanceof Player player)) {
                    send(sender, "<red>Only players can execute this command.");
                    break;
                }

                ItemStack perk = player.getInventory().getItemInMainHand();
                ItemStack appliedItem = player.getInventory().getItemInOffHand();


                if (appliedItem.getType() == Material.AIR || perk.getType() == Material.AIR) {
                    send(sender, "<red>You must hold a perk in your main hand and an item in your off-hand!");
                    break;
                }


                if (!SmithingRegisterAllItem.getAllMaterials().contains(appliedItem.getType())) {
                    Component message = ChatUtil.createHoverableMaterialList();
                    sender.sendMessage(message);
                    break;
                }


                ItemMeta perkMeta = perk.getItemMeta();
                if (perkMeta == null || !perkMeta.getPersistentDataContainer().has(PDCBuilder.PERK_ID, PersistentDataType.STRING)) {
                    send(sender, "<red>The item in your main hand is not a valid perk!");
                    break;
                }

                String perkId = perkMeta.getPersistentDataContainer().get(PDCBuilder.PERK_ID, PersistentDataType.STRING);


                ItemStack updatedItem = plugin.pdcBuilder.addPerkToItem(appliedItem, perkId);
                player.getInventory().setItemInOffHand(updatedItem);


                if (perk.getAmount() > 1) {
                    perk.setAmount(perk.getAmount() - 1);
                } else {
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                }

                send(sender, "<green>Perk successfully applied!");
            }
        }
    }
    private void give(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            send(sender, "<red>Commande réservée aux joueurs.");
            return;
        }
        if (args.length < 2) {
            send(sender, "<gray>Usage : <yellow>/perk give <perk|slot_expander> [nombre] [niveau]");
            return;
        }

        int count = 1;
        if (args.length >= 3) {
            try {
                count = Math.clamp(Integer.parseInt(args[2]), 1, 64);
            } catch (NumberFormatException e) {
                send(sender, "<red>Nombre invalide : " + args[2]);
                return;
            }
        }

        // Item d'extension de slots
        if (args[1].equalsIgnoreCase(EXPANDER_ID)) {
            for (int i = 0; i < count; i++) player.getInventory().addItem(plugin.slotExpander.getItem());
            return;
        }

        AbstractPerk perk = plugin.perkManager.getPerk(args[1]);
        if (perk == null) {
            send(sender, "<red>Perk inconnu : " + args[1]);
            return;
        }
        int level = 1;
        if (args.length >= 4) {
            try {
                level = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                send(sender, "<red>Niveau invalide : " + args[3]);
                return;
            }
            if (level < 1 || level > perk.getMaxLevel()) {
                send(sender, "<red>Niveau entre 1 et " + perk.getMaxLevel() + " pour ce perk.");
                return;
            }
        }
        for (int i = 0; i < count; i++) {
            ItemStack item = perk.getItem(level);
            player.getInventory().addItem(item);
        }
    }

    private void send(CommandSender sender, String message) {
        sender.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    @Override
    public ArrayList<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NonNull [] args) {
        ArrayList<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("reload");
            completions.add("give");
            completions.add("slot");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("slot")) {
            completions.add("init");
            completions.add("apply");
        }
        if (args.length == 2 &&  args[0].equalsIgnoreCase("give")) {
            plugin.perkManager.getPerks().forEach(perk -> completions.add(perk.getId()));
            completions.add(EXPANDER_ID);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            completions.addAll(List.of("01", "02", "04","08","16","32","64"));
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            AbstractPerk perk = plugin.perkManager.getPerk(args[1]);
            if (perk != null) {
                for (int i = 1; i <= perk.getMaxLevel(); i++) completions.add(String.valueOf(i));
            }
        }
        return completions;
    }
}
