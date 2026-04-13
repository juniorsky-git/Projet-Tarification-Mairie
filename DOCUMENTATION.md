# Documentation Technique - Système de Tarification

## 1. Architecture des Données
Le système a été refondu pour éliminer les fichiers CSV et s'appuyer exclusivement sur une structure de données consolidée.

### Source Unique de Vérité
- **Fichier** : `CALC DEP (3).xlsx`
- **Onglet** : `syntheses charges`
- **Données extraites** :
  - **Lignes 4-21** : Dépenses réelles ventilées par nature (Personnel, Fluides, Prestataires, etc.).
  - **Lignes 30-39** : Effectifs annuels par tranche (A à G + EXT) et tarifs unitaires par pôle.

## 2. Moteur de Calcul (`Calculateur.java`)

### Structure `SyntheseGlobale`
Pour garantir la cohérence des chiffres, l'application charge l'intégralité de l'onglet de synthèse dans un cache mémoire au démarrage. Elle fait correspondre les dépenses de chaque pôle avec ses propres effectifs et tarifs sur la base des mêmes index de colonnes (C à H).

### Logique Métier (Multiplicateurs)
Les recettes sont calculées annuellement sur la base de multiplicateurs validés avec les services municipaux :
- **Restauration** : Tarif × Enfants × **140 jours**.
- **Études / Périscolaire** : Tarif × Enfants × **10 mois**.
- **Autres (Loisirs, Ados, Séjours)** : Tarif × Enfants × **1** (base forfaitaire annuelle).

## 3. Logique d'Affichage (`ConsoleUI.java`)
La méthode `afficherDashboardPole` est désormais générique. Elle affiche systématiquement :
- Le détail des charges par **Nature** (ex: Personnel, Électricité).
- Le **Total des dépenses réelles**.
- Le **Total des recettes théoriques** calculées.
- Le **Taux de couverture** (Recettes / Dépenses).

## 4. Maintenance Évolutive
L'application est conçue pour être "Data-Driven" :
- Pour ajouter une nouvelle catégorie de dépense (ex: "Entretien"), il suffit de l'ajouter dans l'onglet Excel entre les lignes 4 et 21. L'application l'affichera automatiquement dans le dashboard concerné.
- Pour modifier les tarifs, changez simplement les valeurs dans les tranches correspondantes de l'Excel.

## 5. Compilation et Exécution
Utilisez le script `build.ps1` ou la commande manuelle incluant les bibliothèques Apache POI dans le classpath.
