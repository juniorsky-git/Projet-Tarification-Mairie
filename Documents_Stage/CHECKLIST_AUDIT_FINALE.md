# Checklist Finale de Vérification d'Audit

Ce document sert de guide pour les prochains audits de fluides réalisés avec l'outil Java.

## ✅ Vérifications Automatiques
- [ ] Lancer `AnalytiqueFluideService` sur le nouveau fichier Excel.
- [ ] Vérifier la présence du dossier `logs_audit/`.
- [ ] Contrôler `audit_gaz.log` : vérifier qu'aucun montant n'est répété anormalement.
- [ ] Contrôler `audit_electricite.log` : vérifier que seules les factures de l'année en cours (2025) sont présentes.

## 🔎 Vérifications Manuelles (Sites Critiques)
- [ ] Comparer le total "Ruelle Saint-Pierre" avec la facture papier.
- [ ] Comparer le total "Gymnase Palestre" pour s'assurer que les multi-compteurs sont bien additionnés.

## 📁 Archivage
- [ ] Renommer le dossier de logs avec la date du jour (ex: `logs_audit_2026_04_24`).
- [ ] Sauvegarder les logs sur le Cloud de la mairie.
