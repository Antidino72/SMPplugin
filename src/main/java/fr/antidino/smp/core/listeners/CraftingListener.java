package fr.antidino.smp.core.listeners;

import fr.antidino.smp.CorePlugin;

import fr.antidino.smp.core.SmithingRegisterAllItem;
import fr.antidino.smp.core.manager.CooldownManager;
import fr.antidino.smp.core.manager.SlotManager;
import fr.antidino.smp.core.manager.perk.AbstractPerk;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class CraftingListener implements Listener {

    private static final Logger log = LoggerFactory.getLogger(CraftingListener.class);
    private final CorePlugin plugin;
    private  final SlotManager slotManager;
    private final CooldownManager cooldownManager;
    private final MiniMessage mm = MiniMessage.miniMessage();
    public CraftingListener(CorePlugin plugin) {
        this.plugin = plugin;
        this.slotManager = new SlotManager(plugin);
        this.cooldownManager = new CooldownManager();
    }

    @EventHandler
    public void onCraftingEvent(CraftItemEvent event) {
        ItemStack resultType = event.getInventory().getResult();

        if (!(event.getWhoClicked() instanceof Player player)) return;


        if (resultType == null || !resultType.hasItemMeta()) return;

        String id = plugin.perkManager.getPerkId(resultType);
        UUID playerId = player.getUniqueId();
        if (id == null) return;
        int number_of_perk = plugin.databaseProvider.NumberOfPerkCrafted(id);
        AbstractPerk perk = plugin.perkManager.getPerk(id);
        log.info("Crafting perk " + id + " " + number_of_perk);
        log.info("Crafting perk " + id + " " + perk.getMaxPossibleCraft());
        if (perk.getMaxPossibleCraft() <= number_of_perk && perk.getMaxPossibleCraft() != -1) {
            event.setCancelled(true);
            player.sendMessage("<red> You can't craft this item!");
        }
        
        if (!event.isCancelled()) {
           if (cooldownManager.hasCooldown(playerId)) {
               event.setCancelled(true);
               player.sendMessage(mm.deserialize("<red>Please wait before crafting this item again!"));
               return;
           }
           cooldownManager.setCooldown(playerId,0.3);
            Bukkit.getScheduler().runTaskAsynchronously(plugin,()->{
                    plugin.databaseProvider.InsertPerkCraftSQL(id,playerId.toString());
            });
        }


        for (Material material : SmithingRegisterAllItem.getAllMaterials()) {
            if (material == resultType.getType()) {
                ItemStack item = event.getRecipe().getResult();
                item = slotManager.initializeSlots(item);
                event.getInventory().setResult(item);
                break;
            }
        }
    }
}