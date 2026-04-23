# 📓 Journal des modifications (CHANGELOG)

Toutes les modifications notables de ce projet seront documentées dans ce fichier.

---

## [1.1.0] - 2026-04-23

### ✨ Features (Fonctionnalités)
- **feat(engine)**: Industrialisation du moteur d'analyse 2025 avec scanner flexible (150 colonnes).
- **feat(ux)**: Implémentation du scoring de diagnostic en 4 couleurs (Performance, Maîtrise, Surveillance, Critique).
- **feat(ux)**: Ajout d'une légende pédagogique d'aide à la décision sur le dashboard.
- **feat(docs)**: Création d'un guide d'entretien exhaustif pour la soutenance de stage.

### 🐛 Fixes (Corrections)
- **fix(compilation)**: Résolution de l'erreur de variables dupliquées (`totalConso`, `totalReel`) dans `AnalytiqueFluideService`.
- **fix(calcul)**: Correction du tarif Gaz (conversion m3 vers kWh) et intégration de l'assainissement pour l'Eau.
- **fix(ui)**: Nettoyage des balises HTML en double dans `index.html`.

### 🛡️ Audit
- **audit(vba)**: Création du script `Audit_Verification_VBA.bas` pour la validation croisée des données.

---

## [1.0.0] - 2026-04-21
- Première version stable de l'outil de tarification municipale.
