package fr.antidino.smp.modules.perk;

/**
 * Entrée stockée dans la liste de slots d'un équipement.
 * Format : "id" (niveau 1, compatible avec les anciens items) ou "id:niveau" (niveau 2+).
 */
public record PerkEntry(String id, int level) {

    public static PerkEntry parse(String raw) {
        if (raw == null) return new PerkEntry("", 1);
        int i = raw.lastIndexOf(':');
        if (i > 0) {
            try {
                int level = Integer.parseInt(raw.substring(i + 1));
                return new PerkEntry(raw.substring(0, i), Math.max(1, level));
            } catch (NumberFormatException ignored) {
                // pas un niveau, on garde la chaîne telle quelle
            }
        }
        return new PerkEntry(raw, 1);
    }

    public String serialize() {
        return level <= 1 ? id : id + ":" + level;
    }
}
