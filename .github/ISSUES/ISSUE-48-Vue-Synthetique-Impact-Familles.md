# Issue #48 — Vue synthétique multi-tranches + amélioration typographique du dashboard

## Contexte
Suite aux retours du maître de stage lors de la revue de l'outil de tarification (version bêta — Mairie de Crosne).

## Problèmes identifiés

### 1. Taille de police trop petite sur le dashboard
La taille de police de base du dashboard était trop petite (14px implicite), rendant le contenu difficile à lire sur un écran à résolution normale (100%). Les cellules de tableau, les sous-titres et les en-têtes de colonnes étaient particulièrement concernés.

### 2. Manque de vue globale dans l'onglet "Impact Familles"
L'onglet "Impact Familles" ne permettait d'analyser qu'une seule tranche tarifaire à la fois. Il était impossible de comparer rapidement l'impact d'une hausse tarifaire sur **toutes les tranches simultanément**.

## Modifications apportées

### `style.css` — Refonte typographique
| Élément      | Avant      | Après      |
|-------------|-----------|-----------|
| `body`      | 14px (implicite) | **15px** |
| `h1`        | 1.875rem   | **2rem**   |
| `.subtitle` | hérité     | **0.95rem** |
| `th`        | 0.75rem    | **0.78rem** |
| `td`        | 0.9rem     | **0.95rem** |
| `.nav-item` | 0.9rem     | **0.92rem** |

### `index.html` — Double vue dans l'onglet "Impact Familles"

#### Vue Détail (mode existant, inchangé)
- Sélecteur de tranche (A → G)
- Curseur de hausse tarifaire (0% → 20%)
- 4 KPI cards : Facture base / Facture simulée / Surcoût mensuel / Surcoût annuel
- Tableau détaillé par prestation avec tarif unitaire, facture base et simulée, surcoût

#### Vue Synthétique (nouveau)
- Curseur de hausse partagé avec la Vue Détail (synchronisés en temps réel)
- **Tableau croisé** avec :
  - **Lignes** = toutes les prestations regroupées par pôle (Mercredis, Périscolaire, Loisirs 5j, Séjours...)
  - **Colonnes** = Quantité mensuelle + 9 tranches (A, B, B2, C, D, E, F, F2, G)
  - **Cellules** = Montant mensuel = Tarif unitaire × Quantité mensuelle × (1 + hausse %)
  - **Coloration** : 🔴 tarif le plus élevé, 🟢 tarif le plus bas par ligne
  - **Ligne TOTAL** : somme mensuelle complète par tranche
- Les deux curseurs (Vue Détail ↔ Vue Synthétique) sont synchronisés

## Branche
`feature/impact-familles-v2`

## Labels suggérés
- `enhancement`
- `ui`
- `impact-familles`
