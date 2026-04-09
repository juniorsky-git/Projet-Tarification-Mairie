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
- l’analyse métier sur Excel a déjà été réalisée,
- les tableaux de tarification ont été construits,
- la partie développement Java commence avec la lecture des fichiers Excel.

---

## Évolutions possibles

Par la suite, le projet pourra être enrichi avec :
- l’automatisation des calculs de tarification,
- la simulation de nouvelles grilles tarifaires,
- une interface utilisateur,
- l’import automatique de nouveaux fichiers.

---

## Auteur

Projet réalisé dans le cadre d’un stage de BUT 2 Informatique.
