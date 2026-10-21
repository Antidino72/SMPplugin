package fr.antidino.smp.core.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {


    private final Map<UUID, Double> cooldowns = new HashMap<>();

    /**
     * Applique un cooldown à un joueur pour une durée donnée en secondes.
     */
    public void setCooldown(UUID playerId, double seconds) {
        Double expireTime = System.currentTimeMillis() + (seconds * 1000.0);
        cooldowns.put(playerId, expireTime);
    }

    /**
     * Vérifie si le joueur est encore en cooldown.
     * Renvoie true si le cooldown est actif, false s'il est expiré.
     */
    public boolean hasCooldown(UUID playerId) {
        if (!cooldowns.containsKey(playerId)) {
            return false;
        }

        Double expireTime = cooldowns.get(playerId);
        if (System.currentTimeMillis() >= expireTime) {
            cooldowns.remove(playerId);
            return false;
        }

        return true;
    }

    /**
     * Renvoie le temps restant en secondes avant la fin du cooldown.
     */
    public int getRemainingSeconds(UUID playerId) {
        if (!cooldowns.containsKey(playerId)) return 0;

        double timeLeft = cooldowns.get(playerId) - System.currentTimeMillis();
        return timeLeft > 0 ? (int) (timeLeft / 1000) : 0;
    }
}