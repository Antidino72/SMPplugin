package fr.antidino.smp.core.manager.perk;
import fr.antidino.smp.CorePlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractPerk {

    protected final String id;
    protected final String displayName;
    protected final CorePlugin plugin;
    protected final NamespacedKey key;
    protected final ItemStack item;

    public AbstractPerk(String id, String displayName, CorePlugin plugin, String keyName, ItemStack item) {
        this.id = id;
        this.displayName = displayName;
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, keyName);
        this.item = item;
    }

    public String getId() {
        return id;
    }
    public ItemStack getItem() {
        return item;
    }

    public String getDisplayName() {
        return displayName;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public abstract ItemStack onApply(ItemStack item);
    public abstract void onRemove(Player player, ItemStack item);
}