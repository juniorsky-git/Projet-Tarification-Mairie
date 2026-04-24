# Issue : Optimisation de la logique Anti-Doublons

## Description
Le système `Mirror` de l'Excel (colonnes répétées trois fois) et le cas des multi-facturations sur une même période nécessitaient une clé d'unicité plus fine.

## Tâches réalisées
- [x] Implémentation d'un `HashSet` basé sur une clé composite `Période + Montant`.
- [x] Distinction entre les répétitions inutiles et les factures légitimes sur le même mois.
- [x] Harmonisation de la logique sur les modules Gaz et Électricité.

## Bénéfices
- Suppression des doublons à 100%.
- Réintégration des factures manquantes (ex: Gymnase Palestre) qui avaient la même date mais des montants différents.
