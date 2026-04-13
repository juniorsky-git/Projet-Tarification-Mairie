# Changelog

## Version actuelle
- Intégration complète des calculs par pôle basée sur `CALC DEP (3).csv` et `CALC DEP (3).xlsx`.
- Ajout de la prise en charge des tableaux de bord suivants :
  - Restauration
  - Accueil de Loisirs
  - Accueil Périscolaire
  - Études Surveillées
  - Espace Ados
  - Séjours
- Nouveau menu : `Rapport COMPLET par PÔLE`.
- Mise à jour de la source principale de données : le projet utilise désormais `Donnees/Autres/CALC DEP (3).csv` en priorité.
- Ajout de méthodes pour :
  - calculer les dépenses par pôle
  - calculer les coûts moyens par enfant
  - générer des résultats financiers par pôle
  - calculer des ratios dépenses/recettes
- Correction du calcul du total des séjours pour refléter précisément le montant `107127,71 €`.
- Amélioration de la robustesse de parsing des montants CSV avec gestion des formats français.
