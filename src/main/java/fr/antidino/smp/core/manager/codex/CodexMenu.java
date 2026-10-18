package fr.antidino.smp.core.manager.codex;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.manager.perk.AbstractPerk;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Menus du /perks codex :
 *  - LIST   : tous les perks actifs du serveur (paginé)
 *  - DETAIL : craft + description d'un perk
 */
public class CodexMenu {

    public static final int PERKS_PER_PAGE = 45;

    // Menu détail (27 slots) : grille de craft en colonnes 1-3
    public static final int[] CRAFT_SLOTS = {1, 2, 3, 10, 11, 12, 19, 20, 21};
    public static final int ARROW_SLOT = 13;
    public static final int RESULT_SLOT = 14;
    public static final int DESCRIPTION_SLOT = 16;
    public static final int BACK_SLOT = 26;

    // Menu liste (54 slots) : barre du bas
    public static final int PREV_SLOT = 45;
    public static final int CLOSE_SLOT = 49;
    public static final int NEXT_SLOT = 53;

    public enum Type { LIST, DETAIL }

    public static class CodexHolder implements InventoryHolder {
        public final Type type;
        public final int page;
        public final String perkId;
        private Inventory inventory;

        public CodexHolder(Type type, int page, String perkId) {
            this.type = type;
            this.page = page;
            this.perkId = perkId;
        }

        void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }
    }

    private final CorePlugin plugin;

    public CodexMenu(CorePlugin plugin) {
        this.plugin = plugin;
    }

    /** Liste triée de façon stable (même ordre à l'ouverture et au clic). */
    public List<AbstractPerk> getSortedPerks() {
        List<AbstractPerk> perks = plugin.perkManager.getPerks();
        perks.sort(Comparator.comparing(AbstractPerk::getId));
        return perks;
    }

    // ------------------------------------------------------------------ LISTE

    public void openList(Player player, int page) {
        List<AbstractPerk> perks = getSortedPerks();
        int maxPage = Math.max(0, (perks.size() - 1) / PERKS_PER_PAGE);
        page = Math.max(0, Math.min(page, maxPage));

        String title = "<dark_gray>Codex des perks";
        if (maxPage > 0) title += " <gray>(" + (page + 1) + "/" + (maxPage + 1) + ")";

        CodexHolder holder = new CodexHolder(Type.LIST, page, null);
        Inventory inv = Bukkit.createInventory(holder, 54, mm(title));
        holder.setInventory(inv);

        int start = page * PERKS_PER_PAGE;
        for (int i = 0; i < PERKS_PER_PAGE && start + i < perks.size(); i++) {
            inv.setItem(i, listItem(perks.get(start + i)));
        }

        ItemStack filler = filler();
        for (int s = 45; s < 54; s++) inv.setItem(s, filler);
        if (page > 0) inv.setItem(PREV_SLOT, button(Material.ARROW, "<yellow>← Page précédente"));
        if (page < maxPage) inv.setItem(NEXT_SLOT, button(Material.ARROW, "<yellow>Page suivante →"));
        inv.setItem(CLOSE_SLOT, button(Material.BARRIER, "<red>Fermer"));

        player.openInventory(inv);
    }

    private ItemStack listItem(AbstractPerk perk) {
        ItemStack item = perk.getItem().clone();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.addAll(descriptionLines(perk));
        lore.add(Component.empty());
        lore.add(mm("<yellow>▶ Clic pour voir le craft"));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    // ----------------------------------------------------------------- DETAIL

    public void openDetail(Player player, AbstractPerk perk, int fromPage) {
        CodexHolder holder = new CodexHolder(Type.DETAIL, fromPage, perk.getId());
        Inventory inv = Bukkit.createInventory(holder, 27, mm("<dark_gray>Craft : ").append(mm(perk.getDisplayName())));
        holder.setInventory(inv);

        ItemStack filler = filler();
        for (int s = 0; s < 27; s++) inv.setItem(s, filler);

        // Grille 3x3 alignée en haut à gauche
        for (int slot : CRAFT_SLOTS) inv.setItem(slot, null);
        List<String> shape = perk.getCraftShape();
        for (int row = 0; row < Math.min(3, shape.size()); row++) {
            String line = shape.get(row);
            for (int col = 0; col < Math.min(3, line.length()); col++) {
                Material m = perk.getCraftIngredients().get(line.charAt(col));
                if (m == null) continue;
                if (!m.isItem()) m = Material.BARRIER;
                inv.setItem(CRAFT_SLOTS[row * 3 + col], new ItemStack(m));
            }
        }

        inv.setItem(ARROW_SLOT, button(Material.ARROW, "<gray>→"));
        inv.setItem(RESULT_SLOT, perk.getItem().clone());

        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();
        meta.displayName(mm("<gold>Description"));
        List<Component> lore = new ArrayList<>(descriptionLines(perk));
        meta.lore(lore);
        book.setItemMeta(meta);
        inv.setItem(DESCRIPTION_SLOT, book);

        inv.setItem(BACK_SLOT, button(Material.ARROW, "<yellow>← Retour"));

        player.openInventory(inv);
    }

    // ---------------------------------------------------------------- HELPERS

    private List<Component> descriptionLines(AbstractPerk perk) {
        List<Component> out = new ArrayList<>();
        if (perk.getDescription().isEmpty()) {
            out.add(mm("<dark_gray>Aucune description configurée."));
        } else {
            for (String line : perk.getDescription()) out.add(mm(line));
        }
        return out;
    }

    private ItemStack button(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm(name));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack filler() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.empty());
        item.setItemMeta(meta);
        return item;
    }

    /** MiniMessage sans italique par défaut (comme le reste du plugin). */
    private static Component mm(String text) {
        return MiniMessage.miniMessage().deserialize("<!italic>" + text);
    }
}
