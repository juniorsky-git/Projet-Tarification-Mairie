# 📓 Journal des modifications (CHANGELOG)

Toutes les modifications notables de ce projet seront documentées dans ce fichier.

---

## [1.2.1] - 2026-04-27 (patch post Code Review)

### 🐛 Fixes critiques
- **fix(storage) [P1]**: Remplacement de la clé d'index numérique par `codeTranche` dans `localStorage`. Faille identifiée par revue de code automatique (GPT-5.4, confiance 0.94) : si l'API retournait les tranches dans un ordre différent (nouvel Excel, tranche ajoutée/supprimée), les prix simulés étaient réappliqués à la **mauvaise tranche** sans aucune alerte visible.

### 🏗️ Architecture
- **arch(storage)**: Structure localStorage mise à jour : `{ "A": 2.50, "EXT": 3.20 }` au lieu de `{ "0": 2.50, "2": 3.20 }`. La clé est désormais une valeur métier stable fournie par le serveur.
- **arch(js)**: Remplacement de `originalSimData[parseInt(idx)]` par `originalSimData.find(t => t.codeTranche === codeTranche)` — lookup sémantique + comportement fail-safe si une tranche disparaît.

### 📋 Workflow documenté
- **docs**: Ajout de la section §6 dans `RETEX_Issue_41_Simulation.md` et Étape 23 dans `journal_developpement.md` — règle de documentation exhaustive instaurée à partir de cette correction.

### 📂 Fichiers modifiés
- `tarification-api/src/main/resources/static/index.html` — 2 blocs modifiés (lignes 367-377 et bloc restauration dans `chargerSimulationRestauration`)
- `tarification-api/target/classes/static/index.html` — synchronisé
- `.github/ISSUES/RETEX_Issue_41_Simulation.md` — section §6 ajoutée
- `Documents_Gestion/journal_developpement.md` — Étape 23 ajoutée
- `CHANGELOG.md` — version 1.2.1 ajoutée

---

## [1.2.0] - 2026-04-27

### ✨ Features (Fonctionnalités)
- **feat(simulation)**: Persistance des simulations What-If via `localStorage`. Les prix simulés survivent désormais aux rechargements de page (F5) et aux navigations entre onglets.
- **feat(ux)**: Indicateur "Restauré du JJ/MM HH:MM" dans le bandeau du moteur de simulation, affiché automatiquement si une simulation sauvegardée est détectée.
- **feat(ux)**: Fonction `resetSimulation()` dédiée au bouton "Réinitialiser (Excel)" — sépare proprement le flux "reset intentionnel" du flux "navigation normale".

### 🏗️ Architecture
- **arch(storage)**: Choix de stocker uniquement les **overrides utilisateur** (prix par index) plutôt que le snapshot complet de `originalSimData`. Garantit que les données source (Excel) restent la source de vérité même si le fichier est re-importé.
- **arch(js)**: Ajout de 4 helpers JS découplés : `saveSimulationToStorage()`, `loadSimulationFromStorage()`, `clearSimulationStorage()`, `resetSimulation()`.

### 🐛 Fixes (préventifs)
- **fix(simulation)**: Recalcul explicite de la ligne "Total" dans `originalSimData` après restauration depuis localStorage — sans cette correction, les 4 KPI cards affichaient les totaux originaux de l'API au lieu des totaux simulés restaurés.

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
