package fr.antidino.smp.core.listeners;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.SmithingRegisterAllItem;
import fr.antidino.smp.core.manager.CooldownManager;
import fr.antidino.smp.core.manager.SlotManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class CraftingListener implements Listener {

    private final CorePlugin corePlugin;
    private  final SlotManager slotManager;
    private final CooldownManager cooldownManager;
    private final MiniMessage mm = MiniMessage.miniMessage();
    public CraftingListener(CorePlugin plugin) {
        this.corePlugin = plugin;
        this.slotManager = new SlotManager(plugin);
        this.cooldownManager = new CooldownManager();
    }

    @EventHandler
    public void onCraftingEvent(CraftItemEvent event) {
        ItemStack resultType = event.getInventory().getResult();

        if (!(event.getWhoClicked() instanceof Player player)) return;


        if (resultType == null || !resultType.hasItemMeta()) return;

        String id = corePlugin.perkManager.getPerkId(resultType);
        if (id != null && !event.isCancelled()) {

            UUID playerId = player.getUniqueId();

           if (cooldownManager.hasCooldown(playerId)) {
               event.setCancelled(true);
               player.sendMessage(mm.deserialize("<red>Please wait before crafting this item again!"));
               return;
           }
           cooldownManager.setCooldown(playerId,0.3);
           corePlugin.databaseProvider.InsertPerkCraftSQL(id, playerId.toString());
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