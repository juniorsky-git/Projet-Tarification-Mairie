# Issue : Tolérance aux pannes et Qualité des données

## Description
Les fichiers municipaux contiennent de nombreuses erreurs de saisie manuelle (fautes de frappe sur les dates, oublis de mots-clés) qui empêchaient l'automatisation.

## Tâches réalisées
- [x] Création d'un détecteur de dates "souple" (basé sur le motif `/` et `au` au lieu de `du`).
- [x] Correction du bug de l'année `205` (typo Excel traitée comme `2025`).
- [x] Exclusion systématique des colonnes de synthèse (`TOTAL`, `CUMUL`) via filtrage sémantique.
- [x] Application d'un filtre strict sur l'année budgétaire (2025).

## Bénéfices
- Élimination des erreurs de calcul (+1000% d'anomalie résolus).
- Plus besoin de corriger l'Excel manuellement avant l'import.
