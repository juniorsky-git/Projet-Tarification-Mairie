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

## Phase 2 : Automatisation des Calculs (Avril 2026)

L'application a évolué pour intégrer un moteur de calcul métier (`Calculateur.java`). Elle croise désormais les dépenses budgétaires et les effectifs d'usagers.

### Indicateurs générés automatiquement :
- **Coût Moyen du repas** (Dépenses / (Usagers * Jours)).
- **Recettes théoriques par tranche**.
- **Taux de couverture global**.

### Nouveaux problèmes techniques résolus :

#### 1. Format de fichier "faux XLS"
- **Problème** : Le fichier `Detail_des_ecritures_...xls` ne s'ouvrait pas avec les outils Excel standards (POI).
- **Analyse** : Après examen du contenu brut, il s'agissait d'un fichier **SpreadsheetML (XML)** portant l'extension `.xls`.
- **Solution** : Pivot vers le fichier `CALC DEP.xlsx` qui contient les mêmes données au format binaire standard.

#### 2. Décalage des colonnes dans les exports
- **Problème** : Les montants et les services étaient lus à 0.
- **Solution** : Correction des index de recherche (décalage de -1) après analyse avec les scripts d'inspection.

#### 3. Logique de calcul annuelle
- **Problème** : Les dépenses sont annuelles mais les effectifs sont par jour moyen.
- **Solution** : Intégration du **coefficient de 140 jours** (moyenne académique) pour ramener les calculs sur la même base temporelle.

---

## Installation et Exécution (Windows)

Le projet utilise désormais des bibliothèques externes (Apache POI). Pour lancer l'application et voir les résultats :

```powershell
powershell -ExecutionPolicy Bypass -File ./build.ps1 run
```

Les documents détaillés de gestion de projet (plans, tâches, bilans) sont disponibles dans le dossier `/Documents_Gestion`.

---

## Évolutions possibles

Par la suite, le projet pourra être enrichi avec :
- L’analyse automatisée des autres services (Accueil de loisirs, Espace Ados).
- Le calcul des recettes réelles à partir des fichiers de facturation Ciril.
- Une fonctionnalité de simulation d'impact lors d'un changement de tarif.

---

## Auteur

Projet réalisé dans le cadre d’un stage de BUT 2 Informatique.
