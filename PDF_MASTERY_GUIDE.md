# Guide de Maîtrise : Génération PDF avec PDFBox

Ce guide a été conçu pour expliquer de manière exhaustive comment l'application transforme les données financières en documents PDF professionnels. Il s'appuie sur le code implémenté par **Séri-khane YOLOU**.

---

## Pilier 1 : Les Fondations (Les fichiers .jar)

Pour coder des PDF en Java, on ne réinvente pas la roue : on utilise des bibliothèques externes appelées **JARs** (Java ARchive).

### Pourquoi 3 fichiers pour le PDF ?
Dans ton dossier `lib/`, trois fichiers sont essentiels pour que l'export fonctionne :
1. **`pdfbox-2.0.31.jar`** : C'est le cerveau. Il contient toutes les méthodes pour créer le document, ajouter des pages et dessiner.
2. **`fontbox-2.0.31.jar`** : C'est l'imprimeur. Il gère toutes les formes des lettres (les polices comme HELVETICA). Sans lui, PDFBox ne peut pas écrire de texte.
3. **`commons-logging-1.2.jar`** : C'est le greffier. PDFBox l'utilise pour noter en interne ce qu'il fait (utile en cas de bug).

### Comment Java les trouve ?
Grâce au **Classpath** (`-cp`). Quand tu lances ton programme avec `./build.ps1`, Java regarde dans le dossier `lib/` pour "charger" ces cerveaux supplémentaires dans sa mémoire.

---

## Pilier 2 : L'Espace Graphique (L'origine (0,0))

C'est le concept le plus important : **L'axe Y est inversé.**

- Sur un écran classique ou dans Word, (0,0) est en haut à gauche.
- Dans un PDF (norme Adobe), **(0,0) est en bas à gauche de la feuille.**

### Ma technique de "descente"
Pour écrire comme dans un livre (du haut vers le bas), je commence tout en haut de la page (`PAGE_HEIGHT`, soit environ 842 points) et je soustrais des points à chaque ligne :
```java
float y = PAGE_HEIGHT - 50; // On commence 50 points sous le bord haut
// ... après avoir écrit une ligne ...
y -= 20.0f; // On descend de 20 points pour la ligne suivante
```

---

## Pilier 3 : Le Stylo Numérique (`ContentStream`)

Pour écrire sur une page, on ouvre un "flux de contenu" (`PDPageContentStream`). C'est comme si on prenait un stylo.

### Les commandes que j'ai utilisées :
- **`beginText()` / `endText()`** : Obligatoire pour écrire. Cela dit à PDFBox "Attention, je vais envoyer des lettres".
- **`showText("Texte")`** : Écrit réellement la chaîne de caractères.
- **`newLineAtOffset(x, y)`** : Place la pointe de ton stylo sur la feuille aux coordonnées choisies.
- **`addRect(x, y, largeur, hauteur)`** : Dessine un rectangle (pour tes bandeaux bleus ou tes barres de progression).
- **`fill()`** : Remplit le dernier rectangle dessiné avec la couleur actuelle.

---

## Pilier 4 : Ta Logique Métier

Voici comment j'ai transformé tes calculs en visuels :

### Tableaux "Zebra" (Alternance de couleurs)
Pour que tes tableaux de charges soient faciles à lire, j'utilise un booléen `alternate`. 
A chaque ligne de ton tableau Excel, je change la couleur de fond :
- Si `true` -> Gris clair.
- Si `false` -> Blanc.
Cela évite de se tromper de ligne en lisant les montants.

### Indicateurs dynamiques
Pour les indicateurs de performance, le programme change automatiquement la couleur du texte (`VERT`, `ORANGE` ou `ROUGE`) selon le pourcentage de couverture calculé pour alerter visuellement le lecteur.

### Mise à l'échelle du Logo
J'ai ajouté un calcul automatique appelé `scale`. Il détecte la largeur d'origine de ton image `Crosne-LOGO.png` et calcule la hauteur proportionnelle pour que le logo ne soit jamais écrasé ou déformé, peu importe sa taille.

---

Ce guide est maintenant ta référence technique. Si un professeur te demande "Comment as-tu fait pour que le texte ne dépasse pas ?", tu pourras lui parler du **système de coordonnées cartésiennes** et de la gestion de la variable **Y**.
