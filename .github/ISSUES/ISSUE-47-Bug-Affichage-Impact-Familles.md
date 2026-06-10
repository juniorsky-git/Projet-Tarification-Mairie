# Issue #47 : Bug d'affichage — Superposition de l'ancien formulaire sur le simulateur Impact Familles

## 🐛 Description du bug
Lors de la navigation vers l'onglet **Impact Familles**, le nouvel interface "Simulateur d'Impact Tarifaire — Accueil de Loisirs" était partiellement masqué et superposé par le rendu de l'ancien formulaire de simulation (pôles restauration, enfance, jeunesse, séjours, ancien slider et section "Facture Mensuelle").

## 🔍 Cause identifiée
Lors du développement du nouveau simulateur, le remplacement du bloc HTML de l'ancienne page `page-impact-familles` a été effectué en plusieurs passes successives. Cette approche a laissé **126 lignes de balises HTML orphelines** directement dans le `<main>` du document, en dehors de tout `<div class="content-page">`.

Ces balises orphelines étaient systématiquement rendues par le navigateur car elles n'étaient soumises à aucune règle CSS `display: none`, contrairement aux vraies pages dont la visibilité est gérée par la fonction `switchPage()`.

**Structure défaillante (avant correctif) :**
```
<div id="page-impact-familles"> ← Nouveau simulateur correct
  ...
</div>  ← Fermeture correcte ligne 752

← ICI : 126 lignes orphelines rendues visuellement
  <select id="impact-tranche">...</select>
  <div>Pôle Restauration</div>
  <div>Pôle Enfance</div>
  ...
  <div id="impact-res-actuel">0,00 €</div>  ← Ancien résultat

<div id="page-historique">  ← Page suivante
```

## ✅ Correctif appliqué
Suppression chirurgicale des 126 lignes orphelines via l'API `System.IO.File` de PowerShell (.NET), après échec des outils d'édition texte sur les caractères spéciaux (tirets em, espaces insécables).

**Structure corrigée :**
```
<div id="page-impact-familles">  ← Nouveau simulateur
  ...
</div>  ← Ligne 752

<!-- commentaire séparateur -->

<div id="page-historique">  ← Ligne 756, propre
```

**Commits associés :**
- `4d0156e` — `feat(impact): Simulateur What-If Accueil de Loisirs 2025`
- `b2b1582` — `fix(impact): Suppression bloc HTML orphelin - correctif affichage simulateur Loisirs`

## 📋 Leçon retenue / Prévention
Lors du remplacement d'un bloc HTML de grande taille, toujours effectuer le remplacement **en une seule opération atomique** (cibler l'intégralité du bloc d'ouverture à la fermeture) plutôt qu'en plusieurs passes partielles successives, afin d'éviter des résidus de balises non fermées.

## 🔍 Validation (Acceptance Criteria)
- [x] L'onglet "Impact Familles" affiche uniquement le nouveau simulateur Accueil de Loisirs.
- [x] Aucun élément de l'ancien formulaire n'est visible (ni pôles, ni ancien slider, ni section "Facture Mensuelle").
- [x] Le slider de pourcentage et le sélecteur de tranche déclenchent bien le recalcul du tableau.
- [x] La structure HTML entre `page-impact-familles` et `page-historique` est propre (zéro balise orpheline).

## 🏷 Labels
`bug` · `frontend` · `html` · `correctif`

---
*Issue créée le 19/05/2026 — Résolution immédiate (même session de développement)*
