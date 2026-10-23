package fr.antidino.smp.modules.perk.rarity;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public record Rarity(String id, String displayName, String color, boolean colorName, int order) {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    public Component label() {
        return MM.deserialize("<!italic>" + color + displayName);
    }

    public String labelMini() {
        return color + displayName;
    }
}
