# Journal de Développement : Analyse et Vision Technique

Ce document détaille les choix techniques, les réflexions métier et la résolution des problèmes rencontrés lors de la phase d'automatisation.

## 1. Vision et Stratégie d'Analyse
L'objectif n'était pas seulement de lire des fichiers, mais de créer un **outil de synthèse décisionnel**. 

### Le défi du cross-referencing (Croisement de données)
Nous avons dû faire communiquer deux mondes différents :
- **La Comptabilité (Ciril)** : Donne des montants globaux payés.
- **Les Statistiques (Dataviz)** : Donnent des effectifs journaliers.
**Vision choisie** : Utiliser un "coefficient de pont" (140 jours de scolarité) pour transformer des enfants par jour en un volume de repas annuel comparable aux factures comptables.

## 2. Analyse des Écarts Budgétaires (Comparaison 4,42 € vs 3,97 €)

### Le calcul du "Coût réel" (3,97 €)
L'outil a calculé ce chiffre selon la décomposition suivante :
- **Numérateur** : 626 861,31 € (Somme des factures Scolarest filtrées pour le scolaire pur).
- **Dénominateur** : 157 920 repas (1 128 enfants x 140 jours).
- **Résultat** : **3,97 € / repas**.

### Pourquoi cet écart avec la cible de 4,42 € ?
Le programme identifie un **écart budgétaire favorable** de ~71 000 €. 
- **L'hypothèse métier** : Les 4,42 € sont le prix unitaire "négocié" au contrat. Le coût de 3,97 € est le coût "payé" constaté en comptabilité sur l'exercice. 
- **L'intérêt de l'outil** : Cette différence met en évidence que sur l'année 2025, la ville a dépensé moins que son budget prévisionnel théorique.

### Le cas du coût Global (4,58 €)
Pour rappel, avant filtrage des services annexes (Ados, Loisirs, Restaurant Communal), le coût global du pôle restauration s'élevait à 4,58 € (723 264 € de dépenses totales).

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

## 4. Professionnalisation et Qualité du Code (Clean Code)
Dans la dernière phase du projet, une attention particulière a été portée à la **lisibilité** et à la **maintenabilité** du logiciel :
- **Documentation Javadoc** : Chaque classe et chaque méthode est désormais documentée (`/** ... */`) pour expliquer son rôle et ses paramètres (important pour la passation de projet).
- **Formatage standardisé** : Aération des blocs de code (accolades, sauts de ligne dans les getters/setters) pour respecter les conventions professionnelles.
- **Modularisation** : Isolation des scripts de recherche (`outils_diagnostic`) pour garder le code source (`src`) propre et prêt pour la production.

## 5. Vision Multi-Pôles (Extensions futures)
L'utilisateur a demandé d'étendre le Dashboard à tous les services (Ados, Loisirs, Périscolaire).

### Problématique des Recettes Théoriques
Pour chaque nouveau service, la formule de calcul change :
- **Scolaire** : Enfants x Prix Repas x 140 jours.
- **Ados** : Ados x Prix (Journée/Demi-journée) x Nombre de jours de vacances.
- **Périscolaire** : Enfants x Prix (Matin/Soir) x Jours d'école.

### Questionnement sur les Données Source (Effectifs)
> [!IMPORTANT]
> **Le défi des effectifs pour les nouveaux pôles**
> Actuellement, le fichier `Feuille_dataviz.xlsx` ne donne pas le nombre d'enfants par tranche (A, B, C...) pour les Ados ou le Périscolaire. 
> - Sans ces chiffres, nous ne pouvons pas calculer les **Recettes Théoriques** réelles. 
> - **Solution temporaire** : L'outil affichera les **Dépenses Réelles** (Audit comptable) mais indiquera qu'une saisie manuelle des volumes est nécessaire pour obtenir le taux de couverture final de ces services.
