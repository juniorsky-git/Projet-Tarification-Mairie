# Issue : Refactorisation du moteur d'extraction des fluides

## Description
Le moteur initial était trop rigide et ne lisait qu'une seule ligne par bâtiment, ce qui posait problème pour le Gaz où les factures sont empilées verticalement.

## Tâches réalisées
- [x] Implémentation de la lecture verticale (gestion du `siteCourant` sur plusieurs lignes).
- [x] Ajout du balayage horizontal étendu (jusqu'à 200 colonnes).
- [x] Support des structures "en miroir" (données répétées horizontalement).

## Bénéfices
- Capture de 100% des factures, même sur les sites complexes avec plusieurs compteurs.
- Robustesse face aux changements de structure des feuilles Excel.
