# Roadmap de correction et d'optimisation du plugin SMPplugin

## Objectif

Ce document résume les points de friction actuels du plugin, les causes de performance, et la roadmap recommandée pour améliorer l'architecture sans réécrire tout le projet d'un coup.

Le projet a une bonne base structurale, mais plusieurs zones critiques sont encore trop "hot path" et doivent être refactorisées pour rester scalable et maintenable.

---

## 1. Diagnostic rapide

### Points forts

- Structure logique par modules : `core`, `modules/perk`, `modules/spell`, `listeners`, `commands`.
- Séparation des listeners, managers et logique de gameplay.
- Le repo a une vraie intention architecturale, ce qui est bon pour la maintenabilité.
- Les systèmes de perks et de spells sont bien pensés sur le plan conceptuel.

### Points faibles

- `PerkTasks` exécute trop de travail dans une boucle tick-based.
- Le plugin recalcul souvent les niveaux/perks à partir des items PDC.
- Les spells deviennent vite monolithiques et contiennent trop de logique métier dans une même classe.
- Les classes de gestion globale (ex. `CorePlugin`, `PerkManager`, `AbstractPerk`) portent trop de responsabilités.
- Le design est encore orienté "prototype / MVP" plutôt que "système extensible et optimisé".

---

## 2. Problèmes de performance observés

### 2.1 Boucle de tick trop heavy

La classe `PerkTasks` est la plus critique.

Actuellement, le code parcourt les joueurs et répète plusieurs opérations à chaque tick :

- lecture du niveau des perks sur l'armure
- analyse du PDC des items
- traitement de la vision nocturne
- traitement de l'aimant
- traitement du double saut

Cela génère du travail inutile sur chaque tick et est coûteux sur un serveur Minecraft.

### 2.2 Recalcul constant des données armure/perk

Les méthodes comme `getArmorPerkLevels()` et `getEquippedPerk()` relisent souvent les données à partir des items.

Le problème n'est pas seulement que cela est lent ; c'est surtout que cela se répète dans des chemins très critiques, alors que les données ne changent pas souvent.

### 2.3 Les effets visuels et de gameplay sont trop imbriqués

Dans `FlameSpell`, la logique mélange :

- position de départ
- direction
- particules
- collision
- dégâts
- knockback
- cooldown

C'est faisable pour 1 ou 2 sorts, mais ça ne scale pas quand il y a beaucoup de sorts, projectiles et effets.

### 2.4 Architecture trop centralisée

`CorePlugin` agit comme une classe de bootstrap + manager + orchestrateur + lifecycle + données globales.

Ce n'est pas fatal, mais cela limite :

- l'extensibilité
- la testabilité
- le découpage des responsabilités
- la capacité à ajouter des services sans faire grossir la classe de manière incontrôlée

---

## 3. Architecture cible recommandée

L'objectif est de passer à un modèle plus robuste :

- registry / manager
- cache d'état du joueur
- services dédiés
- contexte d'exécution
- scheduler unique pour les effets

### Structure cible

```text
modules/
  perk/
    PerkManager.java
    PerkCache.java
    PerkSnapshot.java
    services/
      NightVisionService.java
      MagnetService.java
      DoubleJumpService.java

  spell/
    SpellManager.java
    SpellRegistry.java
    SpellContext.java
    SpellCooldownService.java
    SpellExecutionService.java
    type/
      FlameSpell.java
      FireballSpell.java
      BeamSpell.java

  listeners/
    InventoryListener.java
    ArmorListener.java
    PlayerJoinListener.java
```

### Principe clé

Le `hot path` ne doit plus reparcourir les items et relire le PDC à chaque tick.

Il faut :

- invalider le cache lors d'un changement d'armure / inventaire / reload
- recalculer les données seulement quand nécessaire
- lire uniquement les données déjà mises en cache pendant le runtime

---

## 4. Roadmap prioritaire

## Phase 1 — Corriger les points de perf bloquants

### Tâches

- Introduire un `PlayerPerkCache` ou `PerkSnapshot`.
- Invalider le cache sur :
  - join
  - leave
  - armor equip / unequip
  - changement d'inventaire
  - reload
- Réduire le travail de `PerkTasks.run()`.
- Remplacer les recalculs complets par un accès au snapshot.
- Limiter les traitements d'aimant et de vision nocturne à des conditions plus strictes.

### Livrables

- `PerkCache` ajouté
- `markDirty()` / invalidation centralisée
- `PerkTasks` ne lit plus les items PDC à chaque tick

---

## Phase 2 — Séparer les responsabilités

### Tâches

- Créer des services séparés pour :
  - `NightVisionService`
  - `MagnetService`
  - `DoubleJumpService`
- Déplacer la logique de compétence hors des classes de gestion globales.
- Rendre `PerkManager` plus "registry" et moins "traitement de tout".
- Faire de `AbstractPerk` une abstraction plus légère.

### Livrables

- plus de classes petites et spécialisées
- logique de gameplay isolée dans ses services
- code plus facile à tester

---

## Phase 3 — Refactor des spells

### Tâches

- Ajouter `SpellContext`.
- Ajouter `SpellCooldownService`.
- Ajouter `SpellExecutionService`.
- Séparer :
  - config du spell
  - état runtime
  - logique d'exécution
  - effets visuels
- Centraliser la gestion des projectiles / effets d'aoe / faisceaux.

### Livrables

- un système de spell extensible
- chaque spell ne gère que son propre comportement
- moins de duplicated logic

---

## Phase 4 — Refactor architecture globale

### Tâches

- Réduire la responsabilité de `CorePlugin`.
- Déplacer les initialisations dans une structure de bootstrap propre.
- Séparer bootstrap, managers, runtime state et lifecycle.
- Mettre en place un vrai service layer si le projet grandit.

### Livrables

- plugin plus propre à maintenir
- initialisation plus stable
- reload plus robuste

---

## 5. Points à corriger dans le code actuel

### `PerkTasks.java`

Priorité absolue.

À faire :

- supprimer les recalculs complets dans `run()`
- utiliser un cache de state joueur
- réduire la fréquence de certains traitements
- séparer les services

### `PerkManager.java`

À faire :

- garder la registry, mais diminuer le travail de parsing dans les chemins fréquents
- ajouter un snapshot par joueur
- ne plus reconstruire les données à partir des items à chaque tick

### `AbstractPerk.java`

À faire :

- séparer config et runtime effect logic
- réduire la taille de cette classe
- garder seulement les éléments centraux à la définition du perk

### `AbstractSpell.java`

À faire :

- corriger la logique de permission et cooldown
- centraliser le cooldown dans un service dédié
- éviter les dépendances directes trop lourdes dans les classes dérivées

### `FlameSpell.java`

À faire :

- séparer le comportement d'impact, la création du projectile et les effets visuels
- utiliser un gestionnaire central de projectiles / effets
- remplacer la logique monolithique par un système plus extensible

---

## 6. Priorités recommandées

### Priorité 1 : performance

1. `PerkTasks`
2. `PerkManager`
3. invalidation de cache

### Priorité 2 : architecture

4. `AbstractPerk`
5. `AbstractSpell`
6. `CorePlugin`

### Priorité 3 : extensibilité

7. `FlameSpell`
8. Spell execution system
9. services centralisés

---

## 7. Verdict final

Le repo est déjà bien parti, mais il a encore un défaut majeur : il est trop dépendant des boucles périodiques et du recalcul de données que l'on ne devrait pas recalculer à chaque tick.

La meilleure stratégie n'est pas de réécrire tout le plugin, mais de faire un refactor ciblé sur :

- cache player state
- invalidation par événements
- services spécialisés
- centralisation des effets
- séparation config/runtime/execution

C'est la bonne voie pour monter le système vers un niveau de maintenabilité et de performance nettement supérieur.

---

## 8. Exemple de plan de refactor en 3 itérations

### Itération 1

- `PerkCache`
- invalidation sur events
- `PerkTasks` plus léger

### Itération 2

- `SpellCooldownService`
- `SpellContext`
- architecture de sorts plus modulaire

### Itération 3

- `CorePlugin` moins chargé
- services d'effets plus centralisés
- architecture production-ready

---

## 9. Conclusion

Le code actuel est correct pour un plugin fonctionnel, mais pas encore pour un système de gameplay qui doit rester performant et extensible.

Le meilleur retour sur investissement est de corriger en priorité :

- `PerkTasks`
- `PerkManager`
- le cache des données liées aux perks
- la séparation des services de spell / cooldown / effects

Si ces points sont traités correctement, le repo pourra passer d'un bon prototype à un plugin beaucoup plus solide et plus facile à évoluer.

Si tu veux, je peux aussi te générer une version encore plus courte, ou une version “README-style” plus propre pour le dépôt.