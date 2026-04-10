# Outil de Tarification Municipale - Ville de Crosne

Ce projet Java a ete developpe par un stagiaire (BUT 2 Informatique) pour automatiser l'analyse financiere des differents services municipaux.

## 1. Fonctionnalites de l'Application

### Dashboard Financier Multi-Poles
L'outil permet de generer des tableaux de bord pour trois services distincts :
- Restauration Scolaire (Louise Michel).
- Centre de Loisirs (Gaveriaux).
- Espace Ados.

Pour chaque pole, l'application calcule automatiquement :
- Le total des depenses TTC a partir des factures comptables de l'exercice 2025.
- Les recettes theoriques previsionnelles basees sur les effectifs par tranche tarifaire.
- Le taux de couverture economique (Recettes / Depenses).

### Grille Tarifaire Dynamique
L'application integre une grille de prix multi-services (Repas, Garde, Ados, Loisirs, Etudes) bassee sur les quotients familiaux (CAF). L'utilisateur peut consulter les tarifs individuels en saisissant simplement un Quotient Familial.

## 2. Structure Technique

### Dossiers du Projet
- src : Fichiers sources Java (Calculateur, Main, Modeles).
- lib : Bibliatheques externes (Apache POI pour la lecture Excel).
- Donnees : Fichiers sources de travail (Extractions Ciril et Dataviz).
- Documents_Gestion : Rapports de calculs, journal de developpement et walkthrough.
- outils_diagnostic : Scripts d'inspection utilises pour auditer les fichiers Excel.

### Bibliothèques Utilisées
- Apache POI v5.3.0 : Manipulation des fichiers .xlsx.
- Log4j2 : Gestion des logs (optionnel).

## 3. Guide de Lancement
Pour compiler et executer l'application, l'utilisateur doit disposer de Java (JDK 25 ou superieur) et utiliser le script de build PowerShell :
./build.ps1 run

## 4. Analyse des Donnees
Le projet repose sur l'exploitation croisee de deux fichiers Excel :
- CALC DEP.xlsx (Synthese des engagements et reservations).
- Feuille_dataviz.xlsx (Volumes d'usagers suivis par le pole enfance).

L'outil permet de determiner avec precision si les tarifs municipaux permettent l'autofinancement des depenses directes de chaque service.
