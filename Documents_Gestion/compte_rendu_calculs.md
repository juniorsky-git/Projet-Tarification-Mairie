# Compte Rendu des Calculs Financiers : Evolution des Indicateurs

Ce document detaille la progression des calculs effectues pour chaque service de la mairie de Crosne. Chaque etape reflete une montee en precision des donnees.

## Phase 1 : Premiere estimation du Cout de Revient (Scolaire)
Le premier calcul s'est concentre sur les factures de restauration reelles.
- **Indicateur de Depenses** : Analyse par l'antenne RESTMICH (Louise Michel).
- **Resultat Initial** : **3,97 euros** par repas.
- **Note Technique** : Ce chiffre represente les factures directes payees au prestataire et l'achat de denrees, sans les charges de fonctionnement de la mairie.

## Phase 2 : Integration du Cout de Reference de la Mairie
Comparaison des resultats avec les etudes financieres internes de la Ville.
- **Cible Municipale** : **4,42 euros** (Audit complet du pole financier).
- **Analyse de l'Ecart** : La difference de 0,45 euro est attribuable aux fluides (electricite, chauffage), a l'entretien des locaux et a la masse salariale non facturable directement en prestation.
- **Parametre de l'Outil** : L'application utilise desormais les 4,42 euros comme multiplicateur de base pour le calcul du taux de couverture.

## Phase 3 : Harmonisation des Effectifs et Volumes (Dataviz)
Correction de l'algorithme de comptage des enfants.
- **Effectifs Initiaux** : 1 058 enfants identifies.
- **Erreur de Comptage** : Identification d'une tranche manquante (EXT - Exterieurs).
- **Effectifs Finaux** : **1 128 enfants** (Volume valide apres correction de l'algorithme).
- **Recettes Previsionnelles Annuelles** : **640 963,40 euros** (Basee sur 140 jours de classe).

## Phase 4 : Dashboard Multi-Poles et Synthèse Finale
L'analyse a ete etendue a l'Espace Ados et au Centre de Loisirs avec votre nouvelle grille tarifaire complète.

### Synthese Comparative du Taux de Couverture (Mars 2025)
L'outil a identifie les resultats suivants apres la refonte multi-services :

1. **Pôle Scolaire** :
   - Depenses : 626 861,31 euros.
   - Recettes : 640 963,40 euros.
   - **Taux de Couverture Final : 102,25 %**.

2. **Pôle Espace Ados** :
   - Depenses : 9 301,34 euros (Isoles via l'antenne RESTCA).
   - Recettes : En attente de repartition par tranche dans le fichier Dataviz.

### Conclusion sur la Methodologie
La "chaine" de calcul montre une stabilite des resultats une fois le passage a 1 128 enfants effectue. Le pole scolaire est economiquement equilibre avec les tarifs 2025 en vigueur. Sans l'identification de la tranche EXT, le taux de couverture aurait ete sous-estime de plusieurs points.

## Phase 5 : Audit de Fiabilite et Transparence des Donnees
Pour valider ces chiffres, une procedure d'audit automatisée a été mise en place.
- **Verification Directe** : L'outil `AnalyseTotale` permet d'extraire ligne a ligne les factures considerees comme "Scolaire" (Ciril) et de les confronter visuellement a la realite comptable.
- **Isolateurs de Services** : Les scripts comme `TestScolarestTotal` ont permis d'isoler la part fixe du prestataire (Scolarest) par rapport aux fournitures diverses, confirmant que le cout de revient total depend a 85% de la prestation externe.
- **Securite de Calcul** : L'algorithme rejette automatiquement les montants nuls ou negatifs incoherents pour eviter de fausser les moyennes.

Ces verifications garantissent que le Dashboard reflete une image fidele de la situation budgetaire de la Ville.
