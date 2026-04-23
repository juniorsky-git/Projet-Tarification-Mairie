# Dossier Technique : Automatisation du Diagnostic Fluides (Eau, Gaz, Élec)

Ce document détaille la conception, l'implémentation et la logique métier de l'outil de diagnostic budgétaire des fluides communaux.

## 1. Architecture de la Solution
L'objectif est de transformer un fichier Excel complexe (Source de vérité de la mairie) en un tableau de bord interactif pour les élus et les services techniques.

```mermaid
graph LR
    A[Excel Source : CALC DEP.xlsx] --> B[AnalytiqueFluideService.java]
    B --> C[API REST JSON]
    C --> D[Dashboard Web Premium]
    D --> E[Alerte Anomalie / Fuite]
```

## 2. Mapping Exhaustif : Du Fichier Source au Code
Voici comment le programme "interprète" vos feuilles de calcul.

### 💧 Onglet : "Conso eau"
| Donnée Excel | Emplacement (Indice Cellule) | Code Java |
| :--- | :--- | :--- |
| **Site / Bâtiment** | Concaténation Col 1, 2, 3 | `getStr(r, 1) + " " + getStr(r, 2) + " " + getStr(r, 3)` |
| **Volume m³ (S1)** | Colonne 7 | `getVal(r, 7)` |
| **Montant € (S1)** | Colonne 8 | `getVal(r, 8)` |
| **Volume m³ (S2)** | Colonne 17 | `getVal(r, 17)` |
| **Montant € (S2)** | Colonne 18 | `getVal(r, 18)` |

**Logique de calcul (Eau)** :
> `Coût Théorique = (Conso_S1 + Conso_S2) × 5,09 € + (Abonnement_Fixe × 2)`

---

### 🔥 Onglet : "CONSO GAZ"
La structure du Gaz est **horizontale** (une série de factures mensuelles). Le programme parcourt la ligne de droite à gauche pour trouver la facture la plus récente.

| Donnée Excel | Emplacement | Logique Algorithmique |
| :--- | :--- | :--- |
| **Site** | Colonne 2 | `getStr(r, 2)` |
| **Volume m³** | Colonne `n` | Boucle `step 4` sur les blocs de facturation |
| **Montant €** | Colonne `n + 1` | Récupère le dernier bloc non-vide |

**Logique "Ultra-Précise"** :
> Le programme utilise un ratio moyen de `1,27 €/m³` plus un calcul d'abonnement lissé sur l'année pour détecter les dérives de consommation.

---

### ⚡ Onglet : "CONSO ELEC"
L'électricité suit une logique de **blocs de 4 colonnes** commençant à l'indice 9.

**Exemple de transcription** :
*   **Source Excel** : Le site "GROUPE SCOLAIRE EUROPE" a une conso de 12379 kWh (Col 9) pour 3295,15 € (Col 10).
*   **Interprétation Code** :
    ```java
    double conso = getVal(r, 9); // Extract 12379.0
    double reel = getVal(r, 10); // Extract 3295.15
    double theorique = conso * 0.31; // Calculate 3837.49
    ```

## 3. Logique de Détection d'Anomalies (Le "Cerveau")
Le programme compare systématiquement la réalité (facture) à la théorie (calcul mathématique).

> [!IMPORTANT]
> **Règle métier du seuil critique** :
> Si `abs(Ecart %) > 20%`, le système lève une **Alerte Anomalie**.
> Cela permet de différencier une simple variation saisonnière d'une véritable fuite d'eau ou d'un compteur défectueux.

## 4. Points de maintenance (Pour la suite)
Le code a été conçu pour être **autonome**. Si vous ajoutez un nouveau bâtiment dans l'Excel :
1.  Le site web le détectera **automatiquement** au prochain chargement.
2.  Aucune modification du code Java n'est nécessaire.
3.  La seule maintenance requise est la mise à jour des constantes de tarifs (`PRIX_EAU_M3`, etc.) si les tarifs municipaux changent en 2026.

---
## 5. Gestion des Anomalies : Le Bug des "Nombres Géants"
Lors du développement, un bug critique a été identifié : certaines factures affichaient des milliards d'euros. 

### Analyse du problème
Le fichier Excel de la mairie utilise des colonnes masquées ou situées très loin à droite pour stocker les **noms de série des compteurs (PCE/PDL)**. Comme ces numéros font plus de 14 chiffres, l'algorithme de parcours automatique les a confondus avec des montants financiers.

### Stratégie de Résolution : La Programmation Défensive
Pour corriger cela, deux niveaux de sécurité ont été implémentés :
1.  **Limitation de portée (Scoping)** : Le programme ne lit plus "toute la ligne", mais s'arrête strictement à la 50ème colonne, garantissant qu'il ne "déborde" pas sur les données techniques.
2.  **Validation de Plausibilité (Sanity Check)** : Ajout d'un seuil de validité. Si une facture mensuelle détectée est supérieure à 100 000 €, le programme considère la donnée comme "parasite" et l'ignore.

> [!TIP]
> Cette approche montre une capacité à traiter les imprévus des données réelles, un aspect crucial de la qualité logicielle.

## 6. Nettoyage des Données (Data Scrubbing) et Ergonomie utilisateur
Pour rendre l'outil exploitable par un non-informaticien, deux améliorations majeures ont été apportées :

### Le problème des "Données Parasites"
Dans les fichiers Excel municipaux, les lignes de **Totaux** et de **Bilans** sont mélangées aux données des bâtiments. Sans filtrage, le Dashboard affichait des sites fictifs comme "TOTAL FACTURATION 2025".
*   **Solution** : Implémentation d'un filtre par mots-clés (`TOTAL`, `BILAN`, `FACTURATION`) qui "nettoie" la liste en amont pour ne garder que les entités physiques (bâtiments).

### Sectorisation par Onglets (Navigation UX)
La liste exhaustive des fluides étant très longue, une navigation par **onglets dynamiques** a été mise en place : [Tous | Eau | Gaz | Électricité].
*   **Approche technique** : Utilisation d'attributs de données HTML5 (`data-type`) et de filtrage JavaScript côté client pour une fluidité maximale sans rechargement de page.

> [!NOTE]
> Cette organisation permet un pilotage budgétaire "sectorisé", facilitant le travail des responsables de chaque pôle d'énergie.

## 7. Bilan Budgétaire Annuel Cumulé (Focus 2025)
Pour offrir une vision réelle de la consommation de la ville, le programme est passé d'un affichage "facture par facture" à un **Bilan Cumulé sur l'Exercice 2025**.

### Challenge : Filtrage du "Bruit" de 2024
Le fichier Excel source contient souvent les restes de l'année précédente (2024) sur les mêmes lignes que 2025.
*   **Technique de Code** : Le programme analyse chaque en-tête de période. S'il ne détecte pas la chaîne "25" ou "2025", la colonne est ignorée. Cela garantit que les statistiques affichées ne concernent que le budget en cours.

### Agrégation Horizontale
Le programme ne s'arrête plus à la première facture trouvée. Il parcourt l'intégralité de la ligne Excel (jusqu'à 60 colonnes) pour **additionner** chaque kWh et chaque Euro dépensé depuis le 1er Janvier 2025.

> [!IMPORTANT]
> **Résultat pour l'utilisateur** : Le Dashboard affiche désormais le **Coût Total Réel de l'année** vs le **Coût Théorique Cumulé**, permettant de voir immédiatement si un bâtiment a déjà dépassé son budget annuel en seulement quelques mois.

## 8. Algorithme de "Scanner Flexible" (Résolution des tableaux irréguliers)
Un problème critique a été découvert lors des tests sur le site **"Restos du Cœur"** : l'Excel n'est pas une grille parfaite. Parfois, des colonnes vides ou des décalages apparaissent entre deux mois.

### L'approche du "Pas Fixe" (Échec)
Initialement, le code sautait de 4 colonnes en 4 colonnes. Si l'Excel insérait une colonne vide à la colonne 25, tout le reste de la ligne était décalé et le programme ne voyait plus les mois suivants (ex: Nov, Oct).

### L'approche "Scanner de Motif" (Succès)
Pour corriger cela, j'ai implémenté un algorithme de **Scanning Flexible** :
*   Le programme lit **chaque cellule** de la ligne une par une (jusqu'à 150 colonnes pour couvrir toute l'année).
*   Dès qu'il détecte le motif textuel d'une facture de 2025 (exemple : une cellule commençant par **"du "** et contenant **"25"**), il considère que c'est le point d'ancrage d'un bloc de facture.
*   Il extrait alors les données relatives à l'énergie (kWh) et au coût (€) situées 2 et 3 colonnes plus loin par rapport à ce point d'ancrage.

> [!TIP]
> Cette technique rend le programme **insensible aux colonnes vides** et aux décalages imprévus dans le fichier Excel source. C'est une preuve de robustesse logicielle indispensable en milieu professionnel.

## 9. Méthodologie de Validation (Audit par Cross-Verification)
Dans un contexte de gestion publique (mairie), l'erreur de calcul n'est pas permise. Pour garantir la fiabilité absolue du Dashboard, une méthodologie d'audit par **Cross-Verification** a été adoptée.

### Double Implémentation : Java & VBA
*   **Moteur Principal (Java)** : Développé pour être le moteur industriel de production.
*   **Outil d'Audit (VBA)** : Un script indépendant développé directement dans Excel pour recalculer les données sans passer par le moteur Java.

### Résultat de l'Audit
En comparant les résultats des deux systèmes sur des cas complexes (comme le site des Restos du Cœur), nous avons pu confirmer une **adéquation parfaite des résultats**. Cette démarche a permis de valider :
1.  La correction de l'algorithme de scanning.
2.  L'exactitude des cumuls annuels 2025.
3.  La robustesse du filtrage des données de 2024.

> [!CAUTION]
> Cette étape de validation est ce qui transforme un simple "projet étudiant" en une **solution fiable pour un service financier municipal**.

## 10. Intelligence Temporelle : Reconstruction de la Plage de Diagnostic
Un diagnostic financier n'a de valeur que s'il est précisément daté. Le dernier défi relevé a été de transformer un affichage ponctuel en une **période de couverture réelle**.

### Algorithme de Bornage Chronologique
Le programme extrait désormais dynamiquement la **date de début** de la première facture trouvée et la **date de fin** de la dernière. 
*   **Logique de Calcul** : En parcourant la ligne d'Électricité ou de Gaz, le système "découpe" les textes des périodes pour identifier les extrêmes.
*   **Affichage métier** : L'interface affiche désormais un badge **CUMUL 2025** suivi de la plage exacte (exemple : "du 01/01/25 au 30/11/25").

## 11. Guide d'Interprétation des Résultats (Analyse Métier)
Le tableau de bord n'est pas qu'un outil de calcul, c'est un **outil d'aide à la décision**. Voici comment interpréter les écarts générés :
*   **Écart Négatif (ex: -20%)** : Sous-consommation ou tarifs négociés avantageux. Performance positive.
*   **Écart Positif Modéré (ex: +5%)** : Situation saine, le réel correspond au théorique.
*   **Écart Positif Critique (ex: +100% et plus)** : **Alerte Rouge**. Indique une anomalie majeure (erreur de facturation, fuite, compteur défaillant).

## 12. Vulgarisation : L'Analogie du "Restaurant"
Pour expliquer la logique du projet à un non-technicien, on utilise l'analogie du restaurant :
1.  **La Commande (Consommation)** : Ce que l'on a réellement consommé.
2.  **Le Coût Théorique (Le Menu)** : Le prix que l'on s'attend à payer en regardant la carte.
3.  **Le Facturé Réel (L'Addition)** : Le montant que le serveur demande de payer.
4.  **Le Diagnostic (La Vérification)** : Si l'addition est 10 fois plus élevée que le menu, on détecte une erreur de facturation.

## 13. Détails Mathématiques et Point de Référence
La formule de l'écart est : `(Réel - Théorique) / Théorique`.
*   **Pourquoi diviser par le Théorique ?** C'est notre **point de référence**. Le coût théorique est "pur" car basé sur un contrat fixe. Le diviser permet d'obtenir un pourcentage, ce qui rend l'erreur comparable, que le bâtiment soit une petite guérite de jardin ou une immense école.

## 14. Origine et Maintenance des Tarifs Énergétiques
Les tarifs utilisés comme constantes dans le code (0.31€ pour l'élec, 0.11€ pour le gaz) sont basés sur :
*   **Électricité** : Tarifs Réglementés de Vente (TRV) 2025, incluant l'acheminement (TURPE) et les taxes (TICFE).
*   **Gaz** : Prix Repère de la Commission de Régulation de l'Énergie (CRE).
*   **Eau** : Tarif moyen de la Régie des Eaux locale (2.10€ / m3).

> [!TIP]
> La structure du code permet une mise à jour facile de ces tarifs en cas de renégociation des contrats municipaux, garantissant ainsi la pérennité de l'application.
