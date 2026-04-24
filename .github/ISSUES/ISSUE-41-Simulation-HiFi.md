# Issue #41 : Implémentation HiFi de l'onglet Simulation Financière

## 🎯 Objectif
Intégrer le design haute-fidélité (HiFi) fourni dans la page "Simulation financière" du tableau de bord. La simulation s'appuiera de manière dynamique sur les données du fichier Excel (onglet "Restauration") pour le calcul des différents ratios (taux de couverture, écarts, recettes/dépenses par tranche).

## 📝 Spécifications fonctionnelles

### 1. Refonte du Layout "Simulation Financière - Restauration"
-  Remplacer l'ancienne "Grille Exhaustive" par une vue centrée sur l'analyse budgétaire d'un pôle ciblé (Restauration dans un premier temps).
-  Ajouter les 4 cartes d'indicateurs globaux (Stat Cards) en haut de page.

### 2. Indicateurs Globaux (Cartes)
Les cartes en haut de page agrègent les données des tranches QF. Elles doivent afficher :
- **Recettes totales** : Somme pondérée par tranche QF (Volume * Prix Facturé).
- **Dépenses totales** : Somme des coûts de traitement (Volume * Coût Réel de Revient).
- **Écart global** : Différence entre Recettes totales et Dépenses totales (Généralement déficitaire en tarification sociale).
- **Taux de couverture moyen** : Ratio global (`Recettes globales / Dépenses globales * 100`).
*Note technique* : La mention "vs N-1" visible sur la maquette sera désactivée/masquée pour le moment car l'historisation N-1 n'est pas encore modélisée côté backend/CSV.

### 3. Tableau Détaillé par Tranche
Ajouter un tableau dynamique avec les colonnes suivantes issues de la `SimulationLigne` de l'API :
- Tranche
- Prix facturé (moyen)
- Coût réel (moyen par repas)
- Nombre d'enfants par tranche
- Dépense annuelle générée
- Recette annuelle générée
- Écart (Déficit ou Excédent)
- Taux de couverture

## 🛠 Technique
- **API Endpoint** : Remplacement de l'appel `/api/tarifs/complet` par `/api/simulation/restauration`.
- **Mécanique JS** : 
  - La fonction javascript lira l'itération JSON pour remplir le `<tbody>`.
  - Des variables d'accumulation feront le total des recettes et dépenses pour populer le Header.
  - Formattage monétaire français (`toLocaleString('fr-FR', {style: 'currency', currency: 'EUR'})`).

## 🔍 Validation attendue (Acceptance Criteria)
- [ ] Le layout correspond structurellement et visuellement au mockup fourni.
- [ ] Les totaux de l'en-tête sont parfaitement alignés avec la somme des lignes du tableau.
- [ ] Les données affichées correspondent rigoureusement au fichier source `CALC DEP(4).csv`.
