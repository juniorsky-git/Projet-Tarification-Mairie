# Compte Rendu des Calculs Financiers : Dashboard Multi-Poles

Ce document synthetise les indicateurs calcules par l'outil pour chaque service municipal audite.

## 1. Analyse Detaillee : Pole Scolaire (Cantine)
Le pole scolaire represente le flux financier principal gere par la plateforme.

### Indicateurs de Dépenses (Ciril)
- Antenne source : RESTMICH.
- Service source : 2-RE.
- Total des depenses directes TTC : 626 861,31 euros.

### Indicateurs de Recettes Théoriques (Dataviz)
- Nombre total d'enfants recenses : 1 128 (incluant la tranche EXT).
- Multiplicateur annuel : 140 jours d'ecole.
- Methode : Somme ponderee par tranche (Nb enfants x Tarif unitaire par tranche).
- Total des recettes previsionnelles : 640 963,40 euros.

### Bilan du Pôle Scolaire
- Taux de couverture : 102,25%.
- Ecart budgetaire : + 14 102,09 euros.
Le service scolaire autofinance ses depenses directes de restauration via les tarifs municipaux en vigueur.

## 2. Analyse Detaillee : Pôle Espace Ados

### Indicateurs de Dépenses
- Antenne source : RESTCA.
- Filtre libelle : "ADOS".
- Total des depenses TTC : 9 301,34 euros.

### Indicateurs de Recettes Théoriques
- Nombre d'usagers detectes : 10 919 (Valeur brute extraite de la ligne 75).
- Taux de couverture calcule : 0,00%.
Note technique : Les recettes theoreticales ne peuvent pas etre calculees precisement car la repartition des ados par tranche tarifaire (A, B, C...) n'est pas renseignee dans le fichier Dataviz actuel.

## 3. Methodologie de Calcul des Tranches
L'application utilise les quotients familiaux (QF) suivis par la CAF pour determiner le tarif :
- Tranche EXT : QF > 18 000 euros (Exterieur).
- Tranche A : QF > 18 000 euros.
- Tranche B : 15 000 a 17 999 euros.
- Tranche B2 : 13 000 a 14 999 euros.
- Tranche C : 11 000 a 12 999 euros.
- Tranche D : 9 000 a 10 999 euros.
- Tranche E : 7 000 a 8 999 euros.
- Tranche F : 5 000 a 6 999 euros.
- Tranche F2 : 3 000 a 4 999 euros.
- Tranche G : Moins de 3 000 euros.
L'outil applique dynamiquement les prix correspondants de la grille multi-services fournie par l'utilisateur.
