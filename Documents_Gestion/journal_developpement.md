# Journal de Développement : Analyse et Vision Technique

Ce document détaille les choix techniques, les réflexions métier et la résolution des problèmes rencontrés lors de la phase d'automatisation.

## 1. Vision et Stratégie d'Analyse
L'objectif n'était pas seulement de lire des fichiers, mais de créer un **outil de synthèse décisionnel**. 

### Le défi du cross-referencing (Croisement de données)
Nous avons dû faire communiquer deux mondes différents :
- **La Comptabilité (Ciril)** : Donne des montants globaux payés.
- **Les Statistiques (Dataviz)** : Donnent des effectifs journaliers.
**Vision choisie** : Utiliser un "coefficient de pont" (140 jours de scolarité) pour transformer des enfants par jour en un volume de repas annuel comparable aux factures comptables.

## 2. Analyse des Écarts Budgétaires (Le cas des 4,42 €)
### Problématique
Le programme détectait un coût réel de **4,58 €/repas** alors que le chiffre officiel du pôle financier était de **4,42 €**.
### Analyse de pensée
Plutôt que de simplement "corriger" le chiffre, nous avons cherché à comprendre l'écart. 
- **Le 4,42 €** est une cible contractuelle (le prix du prestataire).
- **Le 4,58 €** inclut les extras (repas occasionnels, ados, centres) identifiés dans la comptabilité globale par l'outil.
**Décision technique** : Implémenter un système à double lecture dans le Dashboard : afficher la **référence officielle** (4,42 €) tout en alertant sur l'**écart réel constaté** en comptabilité.

## 3. Journal des Problèmes Résolus (Troubleshooting)

### A. L'énigme des 11 enfants manquants (Tranche EXT)
- **Symptôme** : Le total Java indiquait 1117 enfants au lieu de 1128 sur Excel.
- **Cause** : Dans le fichier Excel, la tranche "EXT" était inscrite en colonne A, alors que toutes les autres tranches (A, B, C...) étaient en colonne B. Le programme ne regardait que la colonne B.
- **Solution** : Implémentation d'une logique de "fallback" (repli) : si la colonne B est vide, le programme vérifie automatiquement la colonne A.

### B. L'intrusion des données "Espace Ados"
- **Symptôme** : Le total a soudainement bondi à 12 000 enfants.
- **Cause** : En élargissant la recherche pour trouver "EXT", le programme a continué de lire le fichier sous le tableau principal et a trouvé les 10 900 repas du service Ados.
- **Solution** : Ajout d'une condition d'arrêt (`break`) dès que le mot-clé "Total" est rencontré dans le fichier Excel.

### C. Problème d'encodage (Symboles et Accents)
- **Symptôme** : Affichage de `?` à la place de `€` ou d'`É`.
- **Cause** : Le terminal Windows (CMD) n'utilise pas par défaut l'UTF-8 de Java.
- **Solution** : Remplacement des symboles par du texte clair ("euros", "SANS LIMITE") pour garantir un affichage premium et lisible sur n'importe quel ordinateur de la mairie.
