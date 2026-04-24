# Issue : Relocalisation du moteur de calcul et correction du Build API

## Description
Suite à l'implémentation du budget dynamique, un défaut d'architecture a été identifié : le projet API dépendait d'une classe située hors de son périmètre de compilation (Classpath mismatch). Cela empêchait le démarrage de l'application via Maven.

## Tâches réalisées
- [x] Création de la classe `Calculateur` locale au package `fr.mairie.tarification_api`.
- [x] Migration de la logique d'extraction Excel vers le module Spring Boot.
- [x] Mise à jour des injections de dépendances dans `DonneesBudgetaires.java`.
- [x] Validation de la compilation via `mvn compile`.

## Résultat
- **Stabilité** : Le build est désormais 100% stable.
- **Portabilité** : Le dossier `tarification-api` est maintenant un module autonome pouvant être déployé sur un serveur sans dépendance au dossier racine `src`.
- **Performance** : Accès direct aux méthodes de calcul sans traverser les frontières de packages distants.
