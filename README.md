# Outil de Tarification Municipale - Ville de Crosne

Ce projet Java automatise l'analyse financière des services municipaux en croisant les dépenses réelles et les recettes théoriques au sein d'un moteur unifié.

## 1. Fonctionnalités de l'Application

### Dashboard Financier Multi-Pôles
L'application propose 6 tableaux de bord consolidés :
1. **Scolaire** : Restauration scolaire (1,81 M€).
2. **Loisirs** : Accueil de loisirs (1,59 M€).
3. **Espace Ados** : Diagnostic des charges réelles.
4. **Séjours** : Détail complet des vacances.
5. **Études surveillées** : Suivi du personnel et des fournitures.
6. **Accueil Périscolaire** : Analyse des recettes et dépenses périscolaires.

### Consultation Tarifaire
Un moteur de recherche permet de trouver instantanément le tarif applicable à une famille pour tous les services à partir de son **Quotient Familial (QF)**.

## 2. Structure Technique

### Source de Vérité Unique
- **`Donnees/Autres/CALC DEP (3).xlsx`** : L'onglet **`syntheses charges`** est la source unique de vérité. Il regroupe les dépenses par nature comptable, les effectifs par tranche et les tarifs.

### Logique d'Extraction
Le projet utilise une structure **`SyntheseGlobale`** dans `Calculateur.java` qui charge l'intégralité des données en une seule lecture pour garantir la rapidité et la cohérence des calculs (évite les décalages entre dépenses et recettes).

## 3. Guide de Maintenance
Si vous devez modifier les sources de données :
1. Mettez à jour les montants ou effectifs directement dans l'onglet `syntheses charges` de l'Excel.
2. L'application détectera automatiquement toute nouvelle ligne de dépense insérée entre les lignes 4 et 21.
3. Recompilez et lancez via `./build.ps1` ou `Main.java`.

## 4. Prérequis
- Java 21 ou supérieur (pour la génération PDF).
- Bibliothèques Apache POI et PDFBox (incluses dans `lib/`).

---
**Auteur** : Séri-khane YOLOU (Crosne 2025)  
**Version** : 1.3.0  
**Statut** : Version finale stable incluant l'exportation PDF automatisée.
