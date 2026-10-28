<div align="center">

# ⚔️ SMPplugin

**Des perks craftables pour personnaliser armures, armes et outils sur ton serveur SMP.**

![Paper](https://img.shields.io/badge/Paper-26.3+-blue?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-25-orange?style=for-the-badge)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36?style=for-the-badge)
![Langues](https://img.shields.io/badge/Langues-FR%20%7C%20EN%20%7C%20DE%20%7C%20ES-green?style=for-the-badge)

<a href="./docs/screenshoot/codex-main.png"><img src="./docs/screenshoot/codex-main.png" width="40%" alt="Menu principal du Codex"></a>
<a href="./docs/screenshoot/codex-craft.png"><img src="./docs/screenshoot/codex-craft.png" width="40%" alt="Interface de craft du Codex"></a>

<sub><b>Codex</b> &nbsp;•&nbsp; <b>Détail d'un craft</b></sub>

</div>

---

## ✨ En bref

Tu crafts un **perk**, tu le poses sur un équipement dans la **table de forge**, et il apporte un bonus (parfois accompagné d'un malus). Chaque équipement a un nombre limité de **slots**, que tu peux agrandir avec le **Cristal d'emplacement**.

| | |
|---|---|
| 🎒 **15 perks** | Vie, vitesse, double saut, aimant, fonte auto, sonic boom… |
| 🧩 **Slots** | 1 à 3 au craft, jusqu'à 6 avec le cristal |
| ⭐ **Niveaux** | Fusionne des perks identiques pour monter de niveau |
| 💎 **5 raretés** | Commun → Légendaire, avec annonces serveur |
| 📖 **Codex en jeu** | `/perks codex` : toutes les recettes et descriptions |
| 🔥 **Rechargement à chaud** | `/perk reload`, sans redémarrer |
| 🌍 **4 langues** | `fr_FR`, `en_US`, `de_DE`, `es_ES` |

---

## 🚀 Installation rapide

**Prérequis :** serveur **Paper 26.3+** et **Java 25**. Spigot / CraftBukkit ne sont pas supportés.

```bash
git clone https://github.com/Antidino72/SMPplugin.git
cd SMPplugin
mvn clean package
```

1. Copie `target/smpplugin-1.0-SNAPSHOT.jar` dans le dossier `plugins/` de ton serveur.
2. Démarre le serveur : les fichiers de config sont générés dans `plugins/SMPplugin/`.
3. Teste en jeu :

```text
/perk give health_perk
/perk give slot_expander
/perks codex
```

---

## 🎮 Comment ça marche

```text
 Équipement crafté ──► Slots aléatoires (1 à 3)
        │
        ▼
 Perk crafté ──► Table de forge + équipement ──► Perk appliqué ✅
        │
        ├─ 2 perks identiques en table de craft ──► Niveau supérieur
        └─ Cristal d'emplacement en forge ──► +1 slot (6 max)
```

- 💥 Si l'équipement **se casse**, les perks tombent au sol avec leur niveau.
- 🛡️ Les items de perk sont **indestructibles**, ne despawnent pas, et sont placés dans un coffre (Y = 64) s'ils tombent dans le vide.

---

## 💎 Les perks

| Rareté | Perk | Zone | Effet |
|---|---|---|---|
| ⚪ Commun | `sprinter_perk` | Bottes | +10 % vitesse, -2 vie max |
| ⚪ Commun | `night_vision_perk` | Armure | Vision nocturne |
| 🟢 Peu commun | `health_perk` | Armure | Vie max en plus |
| 🟢 Peu commun | `speed_perk` | Bottes | Vitesse de déplacement |
| 🟢 Peu commun | `armor_perk` | Armure | Armure renforcée |
| 🟢 Peu commun | `miner_perk` | Outil | Efficacité de minage +4 / niv. |
| 🟢 Peu commun | `magnet_perk` | Armure | Attire les items (3 / 5 / 8 blocs) |
| 🔵 Rare | `attack_damage_perk` | Arme | Dégâts d'attaque |
| 🔵 Rare | `attack_speed_perk` | Arme | Vitesse d'attaque |
| 🔵 Rare | `smelter_perk` | Pioche | Fond les minerais minés |
| 🔵 Rare | `double_jump_perk` | Armure | 1 à 3 sauts supplémentaires |
| 🔵 Rare | `no_fall_perk` | Bottes | Annule les dégâts de chute |
| 🟣 Épique | `lifesteal_perk` | Arme | Soigne selon les dégâts infligés |
| 🟣 Épique | `glass_cannon_perk` | Arme | +30 % dégâts, -3 armure |
| 🟡 Légendaire | `warden_perk` | Plastron | Sonic boom (accroupi, main vide) |

> Les épiques sont limités à **2 par équipement**, les légendaires à **1**.

---

## ⌨️ Commandes et permissions

| Commande | Rôle | Permission |
|---|---|---|
| `/perks codex` | Ouvre le codex | `smp.perks.codex` *(tous)* |
| `/perk reload` | Recharge config, perks et crafts | `smp.admin.perk` *(op)* |
| `/perk give <perk\|slot_expander> [nombre] [niveau]` | Donne un perk ou un cristal | `smp.admin.perk` |
| `/perk slot init` | Initialise les slots de l'item en main | `smp.admin.perk` |
| `/perk slot apply` | Applique le perk (main) sur l'item (offhand) | `smp.admin.perk` |

Le bouton **GIVE** du codex nécessite `smp.perks.codex.give` *(op)*.

---

## ⚙️ Configuration

Tout se règle en YAML dans `plugins/SMPplugin/`, avec des textes en [MiniMessage](https://docs.advntr.dev/minimessage/format.html).

| Fichier | Contenu |
|---|---|
| `config.yml` | Langue (`lang`) et nombre de slots (`slot.min` / `slot.max`) |
| `rarity.yml` | Raretés, couleurs, limites, annonces et sons |
| `slot_expander.yml` | Cristal d'emplacement : item, craft, slots max |
| `perks/*.yml` | Un fichier par perk : item, craft, niveaux, valeurs, description |
| `lang/*.yml` | Messages du plugin |

Exemple minimal d'un perk :

```yaml
item:
  material: "COMPASS"
  display_name: "<gray>Magnet"
craft:
  shape: ["IEI", "DCD", "IEI"]
  ingredient: { I: IRON_BLOCK, E: ENDER_PEARL, D: DIAMOND_BLOCK, C: COMPASS }
level:
  max: 3
  upgrade_cost: 2
values:
  radius: [3, 5, 8]
```

---

## 🧱 Stack technique

**Java 25** • **Maven** (JAR shadé) • **Paper API** • **SQLite** + **HikariCP** • **Adventure / MiniMessage**

---

## 📚 Aller plus loin

Toute la documentation détaillée est dans le **[Wiki](https://github.com/Antidino72/SMPplugin/wiki)** :

- 🧭 [Installation](https://github.com/Antidino72/SMPplugin/wiki/Installation) et [premiers pas](https://github.com/Antidino72/SMPplugin/wiki/Premiers-pas)
- 💎 [Référence des perks](https://github.com/Antidino72/SMPplugin/wiki/Perks) et [raretés](https://github.com/Antidino72/SMPplugin/wiki/Raretes)
- ⚙️ [Configuration](https://github.com/Antidino72/SMPplugin/wiki/Configuration)
- 🛠️ [Créer un perk](https://github.com/Antidino72/SMPplugin/wiki/Creer-un-perk) et [architecture](https://github.com/Antidino72/SMPplugin/wiki/Architecture)

---

<div align="center">

Développé par **Antidino** • Vertex Studio

</div>
