package fr.antidino.smp.modules.commands;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.modules.perk.SmithingRegisterAllItem;
import fr.antidino.smp.core.MessageManager;
import fr.antidino.smp.modules.perk.AbstractPerk;
import fr.antidino.smp.core.Utils.ChatUtil;
import fr.antidino.smp.core.Utils.PDCBuilder;
import net.kyori.adventure.text.Component;
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

import java.util.*;

/**
 * /perk reload                              : recharge config, perks et crafts
 * /perk give <perk|slot_expander> [n] [niv] : donne un perk (niveau optionnel) ou l'item d'extension de slots
 */
public class AdminCommands implements CommandExecutor, TabCompleter {
    private static final String EXPANDER_ID = "slot_expander";
    MessageManager messageManager;
    private final CorePlugin plugin;
    public AdminCommands(CorePlugin plugin) {

        this.plugin = plugin;
        messageManager = plugin.messageManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            send(sender, messageManager.get("admin_command_usage"));
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "slot" -> slot(sender, args);

            case "reload" -> {
                int count = plugin.fullReload();
                Map<String, String> vars = new HashMap<>();
                vars.put("count", String.valueOf(count));
                send(sender, messageManager.get("config_reloaded",vars));
            }
            case "give" -> give(sender, args);

            default -> send(sender, messageManager.get("admin_command_usage"));
        }
        return true;
    }
    private void slot(CommandSender sender, String[] args) {
        if (args.length < 2) {
            send(sender, messageManager.get("slot_command_usage"));
            return;
        }
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "init" -> {
                if (!(sender instanceof Player player)) {
                    send(sender, messageManager.get("player_only"));
                    break;
                }
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() == Material.AIR) {
                    send(sender, messageManager.get("slot_item_in_hand"));
                    break;
                }
                if (!SmithingRegisterAllItem.getAllMaterials().contains(item.getType())) {
                    Component message = ChatUtil.createHoverableMaterialList();
                    sender.sendMessage(message);
                    break;
                }
                ItemStack updatedItem = plugin.pdcBuilder.initializeSlots(item);
                player.getInventory().setItemInMainHand(updatedItem);
                send(sender,messageManager.get("init_slot") );
            }
            case "apply" -> {
                if (!(sender instanceof Player player)) {
                    send(sender, messageManager.get("player_only"));
                    break;
                }

                ItemStack perk = player.getInventory().getItemInMainHand();
                ItemStack appliedItem = player.getInventory().getItemInOffHand();


                if (appliedItem.getType() == Material.AIR || perk.getType() == Material.AIR) {
                    send(sender, messageManager.get("apply_perk_no_item"));
                    break;
                }


                if (!SmithingRegisterAllItem.getAllMaterials().contains(appliedItem.getType())) {
                    Component message = ChatUtil.createHoverableMaterialList();
                    sender.sendMessage(message);
                    break;
                }


                ItemMeta perkMeta = perk.getItemMeta();
                if (perkMeta == null || !perkMeta.getPersistentDataContainer().has(PDCBuilder.PERK_ID, PersistentDataType.STRING)) {
                    send(sender, messageManager.get("applied_perk_no_valid"));
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
                send(sender, messageManager.get("applied_perk_success"));
            }
        }
    }
    private void give(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            send(sender, messageManager.get("player_only"));
            return;
        }
        if (args.length < 2) {
            send(sender, messageManager.get("give_usage"));
            return;
        }

        int count = 1;
        if (args.length >= 3) {
            try {
                count = Math.clamp(Integer.parseInt(args[2]), 1, 64);
            } catch (NumberFormatException e) {
                Map<String,String> vars = new HashMap<>();
                vars.put("count", String.valueOf(args[2]));
                send(sender,messageManager.get("give_invalid_number",vars));
                return;
            }
        }

        // Item d'extension de slots
        if (args[1].equalsIgnoreCase(EXPANDER_ID)) {
            for (int i = 0; i < count; i++) player.getInventory().addItem(plugin.perkManager.getSlotExpander().getItem());
            return;
        }

        AbstractPerk perk = plugin.perkManager.getPerk(args[1]);
        if (perk == null) {
            Map<String,String> vars = new HashMap<>();
            vars.put("count", String.valueOf(args[1]));
            send(sender, messageManager.get("give_unknow_perk",vars));
            return;
        }
        int level = 1;
        if (args.length >= 4) {
            try {
                level = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                Map<String,String> vars = new HashMap<>();
                vars.put("count", String.valueOf(args[3]));
                send(sender, messageManager.get("give_invalid_number",vars));
                return;
            }
            if (level < 1 || level > perk.getMaxLevel()) {
                Map<String,String> vars = new HashMap<>();
                vars.put("count", perk.getMaxLevel() + "");
                send(sender,messageManager.get("give_invalid_level_amount",vars));
                return;
            }
        }
        for (int i = 0; i < count; i++) {
            ItemStack item = perk.getItem(level);
            player.getInventory().addItem(item);
        }
    }
    private void send(CommandSender sender,Component message) {
        sender.sendMessage(message);
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
