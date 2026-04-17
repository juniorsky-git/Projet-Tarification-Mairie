# Évolution Architecturale : Du Statique au Dynamique
**Date :** 15 avril 2026  
**Heure :** 17:03  
**Auteur :** Séri-khane YOLOU

---

## 🧐 Problématique Initiale
Lors de la phase 1 du projet, les tarifs étaient intégrés directement dans le code source (`DonneesTarifs.java`). Bien que fonctionnelle pour 2025, cette approche présentait des limites majeures :
- **Rigidité** : Impossible de simuler l'année 2024 ou de préparer 2026 sans modifier le code.
- **Maintenance** : Toute mise à jour de la grille demandait l'intervention d'un développeur pour recompiler le projet.
- **Spécificité** : L'outil était "lié" à une seule mairie et une seule époque.

---

## 🚀 La Vision : "Niveau Supérieur"
L'objectif est de transformer l'outil en un **Moteur Universel de Tarification**. Le logiciel ne "possède" plus les données, il devient un outil capable d'interpréter n'importe quelle source externe.

### Analyse comparative (Avant / Après)

| Caractéristique | AVANT (Version 1.2) | APRÈS (Version 2.0 - Implémentation) |
| :--- | :--- | :--- |
| **Source de vérité** | Code source Java (DonneesTarifs) | Fichiers Excel externes (Multi-sources) |
| **Flexibilité temporelle** | Bloqué sur 2025 | 2023, 2024, 2025, 2029... |
| **Profil Utilisateur** | Développeur requis pour mise à jour | Utilisateurs finaux (fonctionnaires) autonomes |
| **Architecture** | Codage "en dur" (Static) | Architecture agnostique et dynamique |
| **Robustesse** | Dépendante de la relecture humaine | Blindage automatique par parseur Excel |

---

## 🛠 Choix Techniques du Saut Technologique
Pour réaliser cette mutation, nous avons retenu trois piliers :

1. **Le "Contrat" Excel** : Définir un standard de fichier `grille_tarifaire.xlsx`. Si le fichier respecte les colonnes (Tranche \| QF Min \| QF Max \| Tarifs), l'outil peut ingérer n'importe quel contenu.
2. **L'Inversion de Contrôle** : Ce n'est plus le programme qui impose ses chiffres, c'est l'utilisateur qui "injecte" sa propre grille dans le moteur de calcul.
3. **Menu de Sélection Dynamique** : Refonte de l'UI pour permettre le choix à la volée du fichier source lors d'une simulation individuelle.

---

## 🛡 Focus : Blindage & Résilience (Mission de Sécurité)
*Mise à jour du 17 avril 2026*

### 1. Diagnostic des "Trous de Sécurité"
Lors des tests de charge, nous avons identifié que le passage au mode dynamique créait une vulnérabilité : l'impossibilité de garantir la structure d'un fichier externe.

**Solution Java implémentée :**
- Trois verrous de sécurité (Guards) ont été placés dans `DonneesTarifs.java`. Ils vérifient le nombre de feuilles, le nombre de lignes et la cohérence de l'en-tête avant toute tentative de calcul.

### 2. Résilience du Workflow (Auto-Détection JDK)
Un bug environnemental a été résolu : le script de build ne dépend plus d'une version fixe de l'extension Java de l'IDE.
- **Workflow** : Détection -> Échec de Compilation -> Audit du script -> Remplacement du Hardcoding par une recherche dynamique de chemin PowerShell.

---

> [!TIP]
> **Argument de Soutenance** : Cette évolution démontre une compréhension des besoins métier réels. Une mairie doit pouvoir simuler les impacts d'une hausse de tarifs (2026+) par rapport aux années précédentes (2024) sans changer d'outil. C'est le principe de la **pérennité logicielle**.

---
*Document finalisé après blindage complet du système.*
