# Projet Tarification Mairie

## Description

Ce projet a pour objectif de développer un outil permettant d’exploiter automatiquement des fichiers Excel issus d’exports du logiciel métier Ciril.

L’application a pour rôle de lire les données, d’identifier les informations utiles, puis de calculer différents indicateurs liés à la tarification des services municipaux, comme par exemple :
- le coût moyen par service,
- les recettes,
- les écarts entre dépenses et recettes,
- le taux de couverture.

Ce projet s’inscrit dans le cadre d’un stage de BUT 2 Informatique.

---

## Contexte

Dans le cadre de la gestion des services municipaux, la mairie dispose de plusieurs données budgétaires et tarifaires réparties dans différents fichiers Excel.

L’objectif est de faciliter leur exploitation en automatisant une partie du traitement informatique, afin de :
- mieux comprendre les coûts par service,
- analyser la participation financière des usagers,
- préparer à terme des simulations d’évolution tarifaire.

---

## Objectifs du projet

Les objectifs principaux sont les suivants :

1. Lire un fichier Excel exporté depuis Ciril
2. Parcourir les lignes du fichier
3. Identifier les colonnes nécessaires
4. Extraire les données utiles
5. Afficher les données extraites
6. Préparer une base de travail pour de futurs calculs automatisés

---

## Fonctionnalités prévues

- Lecture de fichiers Excel
- Parcours des lignes
- Extraction des données utiles
- Affichage des données en console
- Préparation d’une future automatisation des calculs
- Préparation d’une future simulation tarifaire

---

## Structure du projet

Le projet sera organisé de manière simple, avec par exemple :

- `Main.java` : point d’entrée du programme
- `ExcelReader.java` : lecture du fichier Excel
- `ServiceTarification.java` : représentation des données
- `Calculator.java` : futurs calculs de tarification

---

## Technologies utilisées

- Java
- Apache POI (lecture des fichiers Excel)
- Jira (suivi du projet en méthode Scrum)
- Excel / exports Ciril

---

## Méthode de travail

Le projet est organisé sous forme de tickets dans Jira, avec une logique de backlog et de sprint.

Les premières tâches consistent à :
- lire un fichier Excel,
- comprendre sa structure,
- extraire les données nécessaires,
- afficher les résultats de manière simple.

---

## État actuel du projet

À ce stade :
- L’analyse métier sur Excel a déjà été réalisée.
- Les grilles tarifaires 2025 sont intégrées dans le code.
- La lecture dynamique des fichiers **Excel (.xlsx/.xls)** via Apache POI est opérationnelle.
- Un système d'automatisation (script de build) a été mis en place.

---

## Installation et Exécution (Windows)

Le projet utilise désormais des bibliothèques externes (Apache POI). Pour compiler et lancer le projet simplement sans configuration complexe :

Utilisez le script PowerShell `build.ps1` fourni à la racine :

```powershell
# Pour compiler et lancer l'application d'un coup
powershell -ExecutionPolicy Bypass -File ./build.ps1 run

# Pour compiler uniquement
powershell -ExecutionPolicy Bypass -File ./build.ps1 build

# Pour nettoyer les fichiers de build
powershell -ExecutionPolicy Bypass -File ./build.ps1 clean
```

---

## Notes Techniques & Problèmes Résolus

Lors du développement, plusieurs défis techniques ont été surmontés :

### 1. Gestion des dépendances (Apache POI)
Le projet n'utilisant pas Maven ou Gradle, toutes les dépendances JAR ont été téléchargées manuellement dans un dossier `lib/`. Le script de build gère automatiquement l'inclusion de ces JARs dans le `classpath` lors de la compilation et de l'exécution.

### 2. Automatisation du Build (Makefile vs PowerShell)
- **Problème** : L'installation de l'utilitaire `make` sur Windows a échoué à cause des restrictions de droits (UAC).
- **Solution** : Création d'un script `build.ps1` natif Windows qui remplace le Makefile et offre les mêmes fonctionnalités sans installation requise.

### 3. Sécurité PowerShell (Execution Policy)
- **Problème** : Erreur `UnauthorizedAccess` lors du lancement du script .ps1.
- **Solution** : Utilisation de l'option `-ExecutionPolicy Bypass` pour autoriser l'exécution du script de build local.

---

## Évolutions possibles

Par la suite, le projet pourra être enrichi avec :
- L’analyse des exports réels Ciril (fichiers plus volumineux).
- L’automatisation des calculs de tarification.
- La simulation de nouvelles grilles tarifaires.
- Une interface utilisateur.

---

## Auteur

Projet réalisé dans le cadre d’un stage de BUT 2 Informatique.
