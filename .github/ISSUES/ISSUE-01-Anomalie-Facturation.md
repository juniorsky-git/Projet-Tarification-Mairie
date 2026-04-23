# Issue #1: Détection d'anomalie critique de facturation (Ruelle Saint-Pierre)

**Labels**: `bug` 🔴, `critical` ⚠️, `financial-audit` 💰

---

## 📝 Description
L'outil de Diagnostic Performance Fluides a détecté un écart de **+1004,1%** sur le site "RUELLE SAINT-PIERRE". Le coût théorique calculé (174,79€) est en totale contradiction avec le montant facturé réel (1 929,89€).

## 🕵️ Analyse des données
- **Site** : RUELLE SAINT-PIERRE
- **Consommation annuelle** : 563,84 kWh
- **Coût unitaire moyen** : 0,31 €/kWh
- **Montant payé par la ville** : 1 929,89 €

## ⚡ Reproduction du Diagnostic
1. Lancer l'application `AnalytiqueFluideApplication`.
2. Cliquer sur l'onglet **"Électricité"**.
3. Rechercher le bâtiment **"RUELLE SAINT-PIERRE"**.
4. Constater le badge rouge **"ANOMALIE DÉTECTÉE"**.

## ✅ Actions recommandées
- [ ] Ouvrir une enquête auprès du fournisseur d'énergie.
- [ ] Demander une vérification physique du compteur sur place.
- [ ] Vérifier d'éventuels arriérés de facturation des années précédentes.

---
*Cette issue a été ouverte automatiquement suite au diagnostic de l'exercice budgétaire 2025.*
