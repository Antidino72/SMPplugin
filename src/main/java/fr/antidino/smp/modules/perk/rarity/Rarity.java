package fr.antidino.smp.modules.perk.rarity;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public record Rarity(String id, String displayName, String color, String symbol, boolean colorName,
                     int maxPerItem, String announce, String sound, int order) {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    public String labelMini() {
        return color + (symbol.isEmpty() ? "" : symbol + " ") + displayName;
    }

    public Component label() {
        return MM.deserialize("<!italic>" + labelMini());
    }

    public boolean hasLimit() {
        return maxPerItem >= 0;
    }
}
