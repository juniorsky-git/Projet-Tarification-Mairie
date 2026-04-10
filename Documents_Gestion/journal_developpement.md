# Journal de Developpement : Projet de Tarification Municipale

Ce document retrace l'historique des evolutions techniques apportees a l'outil de calcul financier pour la mairie de Crosne (BUT 2 Informatique).

## 1. Etat Initial et Objectifs
L'application d'origine etait un simple calculateur de prix individuel. L'objectif du stage a ete de transformer cet outil en un dashboard financier complet capable de confronter les depenses reelles (comptabilite Ciril) aux recettes theoriques (statistiques Dataviz).

## 2. Refonte du Moteur de Calcul (Calculateur.java)
L'etape majeure a ete la creation d'un moteur de filtrage intelligent capable de lire les extractions Excel. 

### Gestion des Antennes de Facturation
Le code identifie desormais chaque service via son code antenne dans les depenses :
- RESTMICH : Restauration scolaire (Louise Michel).
- RESTGAV : Restauration Gaveriaux (Centre de loisirs).
- RESTCA : Restauration Ados.
- CRCOLL : Etudes et Periscolaire.

### Algorithme de Filtrage Dynamique
Pour chaque pole, le moteur de calcul applique des regles d'inclusion et d'exclusion (mots-cles dans les libelles de factures). Cela evite de compter deux fois les memes depenses si elles partagent une antenne commune.

## 3. Architecture Multi-Services (Nouveaute Mars 2025)

### Grille Tarifaire Massive
L'utilisateur a fourni une grille de prix detaillee pour tous les services (Ados, Loisirs, Etudes). La structure de donnees a ete migree vers une Map pour permettre une recherche instantanee du prix correct selon l'activite choisie.

### Calcul des Recettes par Pôle
L'outil multiplie le nombre d'usagers par tranche (A, B, C...) par le tarif correspondant de la grille. 
- Pour la restauration scolaire, le calcul est base sur un forfait de 140 jours de classe par an.
- Pour les autres services, le multiplicateur est configurable selon l'usage (journalier ou mensuel).

## 4. Documentation et Standards de Code
Le projet respecte les regles suivantes pour garantir une maintenance facile par les services informatiques :
- Javadoc systematique pour chaque classe et methode.
- Suppression des operateurs ternaires (symboles ?) au profit de structures if/else explicites.
- Aeration du code avec des accolades sur des lignes separees pour une lecture plus fluide.
- Standardisation des textes en ASCII pour eviter les problemes d'affichage dans les terminaux Windows.

## 5. Analyse des Ecarts et Conclusion
L'analyse a revele un taux de couverture de 102,25% pour le pole scolaire (Michali). Cela signifie que les recettes theoriques sont legerement superieures aux depenses directes, confirmant la validite du modele de tarification actuel sur ce perimetre specifique.
