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

### Consultation Interactive
La méthode `consulterTarif` permet une simulation en temps réel :
- **Mode 2025** : Utilise les données de référence intégrées au code.
- **Mode Import Excel** : Permet de charger n'importe quelle grille tarifaire indépendante (au format Table) pour tester des simulations historiques ou des scénarios futurs. Le moteur de parsing extrait automatiquement les tranches depuis le texte descriptif du Quotient Familial.

## 4. Exportation et Rapports PDF
L'application intègre un moteur d'exportation professionnel (`PdfExportService`) utilisant la librairie Apache PDFBox. Ce service automatise la création d'un rapport financier complet.

### Contenu du rapport :
- **Identification** : Page de garde avec bandeau institutionnel Ville de Crosne.
- **Détails Financiers** : Une page par pôle municipal incluant le détail des charges directes.
- **Indicateurs de Performance** : Le taux de couverture est illustré par une barre de progression colorée dynamiquement (Vert ≥ 80%, Orange ≥ 50%, Rouge < 50%).
- **Synthèse Consolidée** : Tableau comparatif global des 6 pôles montrant les dépenses et recettes totales.
- **Grilles Tarifaire** : Intégration des grilles de référence 2025 pour consultation directe.

Pour un détail exhaustif des méthodes de dessin PDF et de la logique algorithmique retenue, consultez mes guides techniques :
- [PDF_EXPORT_GUIDE.md](file:///c:/Users/stagedg2/Projet_mairie_outil_tarification/PDF_EXPORT_GUIDE.md) : Guide d'utilisation et structure.
- [PDF_EXPORT_METHODOLOGY.md](file:///c:/Users/stagedg2/Projet_mairie_outil_tarification/PDF_EXPORT_METHODOLOGY.md) : Analyse approfondie des algorithmes de rendu et de la gestion des images.

## 5. Maintenance et Standards
- **Auteur principal** : Séri-khane YOLOU.
- **Style de Code** : Le code source suit des règles de lisibilité strictes (100% d'accolades, interdiction des ternaires, Javadoc exhaustive).
- **Compilation** : Utilisez Java 21+ avec les bibliothèques Apache POI et PDFBox incluses dans le dossier `lib/`.
