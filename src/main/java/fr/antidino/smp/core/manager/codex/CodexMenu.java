package fr.antidino.smp.core.manager.codex;

import fr.antidino.smp.CorePlugin;
import fr.antidino.smp.core.manager.perk.AbstractPerk;
import fr.antidino.smp.librairie.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Menus du /perks codex :
 *  - LIST : tous les perks actifs du serveur (paginé) + item d'extension de slots
 *  - DETAIL : craft + description (+ niveaux) d'un perk ou de l'item de slots

 * Tous les textes affichés passent par le MessageManager (fichiers de langue).
 */
public class CodexMenu {

    public static final int PERKS_PER_PAGE = 45;
    public static final int GIVE_SLOT = 8;
    // Menu détail (27 slots) : grille de craft en colonnes 1-3
    public static final int[] CRAFT_SLOTS = {1, 2, 3, 10, 11, 12, 19, 20, 21};
    public static final int ARROW_SLOT = 13;
    public static final int RESULT_SLOT = 14;
    public static final int DESCRIPTION_SLOT = 16;
    public static final int BACK_SLOT = 26;

    // Menu liste (54 slots) : barre du bas
    public static final int PREV_SLOT = 45;
    public static final int EXPANDER_SLOT = 47;
    public static final int CLOSE_SLOT = 49;
    public static final int NEXT_SLOT = 53;

    public static final String EXPANDER_ID = "__slot_expander__";

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

    public ItemStack result;

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
        page = Math.clamp(page, 0, maxPage);

        Component title = maxPage > 0
                ? msg("codex_title_paged", "page", String.valueOf(page + 1), "max", String.valueOf(maxPage + 1))
                : msg("codex_title");

        CodexHolder holder = new CodexHolder(Type.LIST, page, null);
        Inventory inv = Bukkit.createInventory(holder, 54, title);
        holder.setInventory(inv);

        int start = page * PERKS_PER_PAGE;
        for (int i = 0; i < PERKS_PER_PAGE && start + i < perks.size(); i++) {
            inv.setItem(i, listItem(perks.get(start + i)));
        }

        ItemStack filler = filler();
        for (int s = 45; s < 54; s++) inv.setItem(s, filler);
        if (page > 0) inv.setItem(PREV_SLOT, button(Material.ARROW, msg("codex_prev_page")));
        if (page < maxPage) inv.setItem(NEXT_SLOT, button(Material.ARROW, msg("codex_next_page")));
        if (plugin.slotExpander != null) inv.setItem(EXPANDER_SLOT, expanderListItem());
        inv.setItem(CLOSE_SLOT, button(Material.BARRIER, msg("codex_close")));

        player.openInventory(inv);
    }

    private ItemStack listItem(AbstractPerk perk) {
        ItemStack item = perk.getItem().clone();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.addAll(descriptionLines(perk.getDescription()));
        if (perk.getMaxLevel() > 1) {
            lore.add(Component.empty());
            lore.add(msg("codex_max_level", "level", AbstractPerk.roman(perk.getMaxLevel())));
        }
        lore.add(Component.empty());
        lore.add(msg("codex_click_craft"));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack expanderListItem() {
        ItemStack item = plugin.slotExpander.getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.addAll(descriptionLines(plugin.slotExpander.getDescription()));
        lore.add(Component.empty());
        lore.add(msg("codex_click_craft"));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    // ----------------------------------------------------------------- DETAIL

    public void openDetail(Player player, AbstractPerk perk, int fromPage) {
        List<Component> description = new ArrayList<>(descriptionLines(perk.getDescription()));
        if (perk.getMaxLevel() > 1) {
            description.add(Component.empty());
            description.add(msg("codex_levels", "max", AbstractPerk.roman(perk.getMaxLevel())));
            description.add(msg("codex_upgrade_cost", "cost", String.valueOf(perk.getUpgradeCost())));
            description.add(msg("codex_upgrade_hint"));
        }
        openRecipeView(player, perk.getId(), fromPage,
                msg("codex_craft_title").append(mm(perk.getDisplayName())),
                perk.getItem().clone(), perk.getCraftShape(), perk.getCraftIngredients(), description);
    }

    public void openExpanderDetail(Player player, int fromPage) {
        if (plugin.slotExpander == null) return;
        List<Component> description = new ArrayList<>(descriptionLines(plugin.slotExpander.getDescription()));
        description.add(Component.empty());
        description.add(msg("codex_expander_usage"));
        description.add(msg("codex_expander_added", "added", String.valueOf(plugin.slotExpander.getAdded())));
        description.add(msg("codex_expander_max", "max", String.valueOf(plugin.slotExpander.getMax())));
        openRecipeView(player, EXPANDER_ID, fromPage,
                msg("codex_craft_title").append(mm(plugin.slotExpander.getDisplayName())),
                plugin.slotExpander.getItem(), plugin.slotExpander.getCraftShape(),
                plugin.slotExpander.getCraftIngredients(), description);
    }

    private void openRecipeView(Player player, String id, int fromPage, Component title, ItemStack result,
                                List<String> shape, Map<Character, Material> ingredients, List<Component> description) {
        CodexHolder holder = new CodexHolder(Type.DETAIL, fromPage, id);

        this.result = result;
        Inventory inv = Bukkit.createInventory(holder, 27, title);
        holder.setInventory(inv);

        ItemStack filler = filler();
        for (int s = 0; s < 27; s++) inv.setItem(s, filler);

        // Grille 3x3 alignée en haut à gauche
        for (int slot : CRAFT_SLOTS) inv.setItem(slot, null);
        for (int row = 0; row < Math.min(3, shape.size()); row++) {
            String line = shape.get(row);
            for (int col = 0; col < Math.min(3, line.length()); col++) {
                Material m = ingredients.get(line.charAt(col));
                if (m == null) continue;
                if (!m.isItem()) m = Material.BARRIER;
                inv.setItem(CRAFT_SLOTS[row * 3 + col], new ItemStack(m));
            }
        }

        inv.setItem(ARROW_SLOT, button(Material.ARROW, mm("<gray>→")));
        inv.setItem(RESULT_SLOT, result);

        if (player.hasPermission("smp.perks.codex.give")) {
            ItemStack give_button = new ItemBuilder(Material.BARRIER, plugin)
                    .setName(msg("codex_give_button"))
                    .build();
            inv.setItem(GIVE_SLOT, give_button);
        }
        ItemStack book = new ItemBuilder(Material.BOOK, plugin)
                .setLore(description)
                .setName(msg("codex_description_title"))
                .build();

        inv.setItem(DESCRIPTION_SLOT, book);
        inv.setItem(BACK_SLOT, button(Material.ARROW, msg("codex_back")));
        player.openInventory(inv);
    }

    // ---------------------------------------------------------------- HELPERS

    private List<Component> descriptionLines(List<String> lines) {
        List<Component> out = new ArrayList<>();
        if (lines.isEmpty()) {
            out.add(msg("codex_no_description"));
        } else {
            // Descriptions définies dans la config des perks (pas dans les fichiers de langue)
            for (String line : lines) out.add(mm(line));
        }
        return out;
    }

    private ItemStack button(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
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

    /**
     * Récupère un message traduit, sans italique par défaut.
     * Les variables sont passées par paires clé/valeur : msg("key", "count", "5", "max", "10")
     */
    private Component msg(String key, String... vars) {
        Component component;
        if (vars.length == 0) {
            component = plugin.messageManager.get(key);
        } else {
            Map<String, String> map = new HashMap<>();
            for (int i = 0; i + 1 < vars.length; i += 2) map.put(vars[i], vars[i + 1]);
            component = plugin.messageManager.get(key, map);
        }
        return component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    /** MiniMessage sans italique par défaut (pour les textes venant de la config des perks). */
    private static Component mm(String text) {
        return MiniMessage.miniMessage().deserialize("<!italic>" + text);
    }
}