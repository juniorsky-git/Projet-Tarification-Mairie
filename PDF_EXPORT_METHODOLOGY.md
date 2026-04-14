# Méthodologie Technique : Moteur de Rendu PDF

Ce document expose les principes algorithmiques et les choix d'implémentation retenus pour le service d'exportation de la mairie de Crosne.

---

## 1. Algorithme de Placement Graphique

Contrairement à un document texte classique, un PDF se gère comme un canvas de dessin. Un système de coordonnées absolues est utilisé pour garantir une mise en page immuable.

### Repère de Coordonnées
Le système PDFBox utilise l'origine (0,0) en bas à gauche de la page. Pour dessiner de manière descendante (plus logique pour un rapport), j'ai implémenté une variable de curseur vertical `y`.
- **En-têtes** : La page de garde dispose d'une zone supérieure blanche pour le logo (haut-gauche) et d'un bandeau bleu institutionnel décalé pour les titres. Sur les pages de pôles, les en-têtes occupent la bande `PAGE_HEIGHT - 33` à `PAGE_HEIGHT - 80`.
- **Corps de page** : Commencent généralement à `PAGE_HEIGHT - 120` et descendent jusqu'à la marge de sécurité de 50 points.

### Logique des Multiplicateurs Annuels
Le calculateur utilise des coefficients pour aligner les recettes sur les dépenses annuelles :
- **Restauration scolaire (x140)** : On multiplie le tarif unitaire par 140 jours, ce qui correspond à la moyenne des jours d'école sur une année complète (zone B).
- **Périscolaire & Études (x10)** : On multiplie les recettes par 10 mois. En effet, ces services s'arrêtent durant les vacances d'été (juillet/août), mais les dépenses de fonctionnement sont calculées sur l'année budgétaire globale. Cet alignement est crucial pour obtenir un taux de couverture honnête.

---

## 2. Logique de Dessin des Tableaux (`dessinerTableauDepenses`)

Pour rendre les tableaux lisibles et esthétiques, j'ai implémenté les mécanismes suivants :

### Alternance de Couleurs (Zebra)
Pour chaque ligne de dépense, j'ai mis en place une condition d'alternance chromatique :
```java
if (alternate) {
    cs.setNonStrokingColor(GRIS_CLAIR);
} else {
    cs.setNonStrokingColor(Color.WHITE);
}
```
Cela permet à l'œil de suivre la ligne de dépense jusqu'au montant associé sans risque de confusion.

### Gestion des Grands Libellés
Les natures de dépenses (comptabilité M14) sont souvent très longues. Pour éviter qu'elles ne s'écrasent sur les montants, j'ai créé la méthode `tronquerTexte()`. Elle assure que tout libellé dépassant une certaine largeur est élégamment coupé avec des points de suspension.

---

## 3. Dynamisme des Indicateurs de Performance

Le rapport ne se contente pas d'afficher des chiffres ; il les interprète visuellement via les Taux de Couverture des charges.

### Code Couleur Sémantique
Le taux de couverture (`Recettes / Dépenses * 100`) est interprété visuellement pour faciliter la prise de décision politique :
- **Vert (≥ 80%)** : Équilibre financier satisfaisant. Les tarifs couvrent la quasi-totalité des charges.
- **Orange (≥ 50%)** : Co-financement modéré. La ville subventionne environ la moitié du service.
- **Rouge (< 50%)** : Subvention municipale majoritaire. Le service est socialement très soutenu par le budget de la commune.

---

## 4. Intégration Logicielle

Le `PdfExportService` est totalement découplé du `Calculateur`. Il interroge les méthodes métier (`calculerTotalDepenses`, `calculerRecettesAnnuelles`) et transforme ces objets Java en flux graphiques PDF (`PDPageContentStream`). Cette modularité permet de faire évoluer le design sans toucher à la logique mathématique.
