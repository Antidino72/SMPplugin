package fr.antidino.smp.librairie;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;

import java.util.*;

public class ChatUtil {
    public static Component createHoverableMaterialList() {
        
        Map<String, List<String>> equipmentMap = new LinkedHashMap<>();

       
        List<String> validTypes = List.of(
                "HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS",
                "SWORD", "AXE", "PICKAXE", "SHOVEL", "HOE", "SHIELD"
        );

        for (Material mat : Material.values()) {
            if (!mat.isItem()) continue;
            String name = mat.name();

            for (String type : validTypes) {
                if (name.endsWith(type)) {
                    String prefix = name.replace("_" + type, "").toLowerCase(Locale.ROOT);
                    if (type.equals("SHIELD")) prefix = "standard";

                    equipmentMap.computeIfAbsent(type, k -> new ArrayList<>()).add(prefix);
                }
            }
        }

      
        StringBuilder hoverBuilder = new StringBuilder();
        hoverBuilder.append("<gold><b>--- ÉQUIPEMENTS ACCEPTÉS ---</b></gold><br><br>");

        equipmentMap.forEach((type, materials) -> {
            hoverBuilder.append("<yellow><b>").append(type).append(" :</b></yellow> <gray>")
                    .append(String.join(", ", materials))
                    .append("</gray><br>");
        });

       
        String fullMessage = "<red>Cet item n'est pas valide ! "
                + "<hover:show_text:'" + hoverBuilder.toString() + "'>"
                + "<gold><u>[Survole ici pour voir la liste]</u></gold>"
                + "</hover>";

        return MiniMessage.miniMessage().deserialize(fullMessage);
    }


}
