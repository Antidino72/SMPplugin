package fr.antidino.smp.modules.perk.papi;

import fr.antidino.smp.CorePlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PapiExpansion extends PlaceholderExpansion {
    private static final Logger log = LoggerFactory.getLogger(PapiExpansion.class);
    private final CorePlugin plugin;
    public PapiExpansion(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "Antidino72"; //
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "smp"; //
    }

    @Override
    @NotNull
    public String getVersion() {
        return "1.0.0"; //
    }


    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        if (params.startsWith("perk_number")) {
            String subParam = params.replace("perk_number_", "");
            return String.valueOf(plugin.dataCache.getPerkCraftCount(subParam));
        }
        return null;
    }


}