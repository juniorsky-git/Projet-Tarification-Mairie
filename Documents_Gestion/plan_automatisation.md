# Automatisation des Calculs et Indicateurs

## Objectif
Implémenter la logique de calcul automatique du **Coût Moyen** et des **Recettes Totales** en croisant les dépenses réelles (Ciril) et les volumes d'usagers.

## Proposed Changes

### 1. Développement du Moteur de Calcul
#### [NEW] [Calculateur.java]
- Méthode `chargerDepensesService(String antenne)` : scanne `depenses centre de loisirs 2025.xlsx` et somme les montants TTC pour le code service donné (ex: `CLMICH`).
- Méthode `chargerVolumesUsagers()` : lit `Feuille_dataviz .xlsx` pour récupérer le nombre d'enfants par tranche (A, B, C...).
- Méthode `calculerSynthese(String antenne)` :
    - Calcule le **Coût Moyen** = `Total Dépenses / (Total Enfants * 140 jours)`.
    - Calcule les **Recettes Totales** = `Somme par tranche(Tarif * Nb Enfants * 140 jours)`.
    - Calcule le **Taux de Couverture**.

### 2. Adaptation du Main pour la démonstration
#### [MODIFY] [Main.java]
- Ajout d'une section "SYNTHÈSE AUTOMATISÉE (CLMICH)".
