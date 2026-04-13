# Outil de Tarification Municipale - Ville de Crosne

Ce projet Java automatise l'analyse financière des services municipaux en croisant les dépenses réelles et les recettes théoriques.

## 1. Fonctionnalités de l'Application

### Dashboard Financier Multi-Pôles
L'application propose désormais 5 tableaux de bord distincts :
1. **Scolaire** : Cantine Louise Michel.
2. **Loisirs** : Centres de loisirs Gaviériaux.
3. **Espace Ados** : Diagnostic des charges réelles (17k€).
4. **Séjours** : Détail par destination (Curie, Brassens, Ados - 107k€).
5. **Audit Eau** : Suivi des consommations et facturations fluides (via scripts de diagnostic).

### Consultation Tarifaire
Un moteur de recherche permet de trouver instantanément le tarif applicable à une famille pour tous les services à partir de son **Quotient Familial (QF)**.

## 2. Structure Technique

### Dossiers Clés
- `src` : Source Java (Calculateur, UI).
- `outils_diagnostic` : **CRITIQUE**. Contient les outils d'audit (Java et Python) permettant de vérifier les données Excel avant intégration.
- `Donnees/Autres/CALC DEP (3).csv` : **Source primaire de données** pour les totaux des dépenses.
- `Donnees/Autres/CALC DEP (3).xlsx` : **Source secondaire** pour les détails de simulation.

### Logique d'Extraction (Maintenance)
Pour les pôles complexes (Ados, Séjours), l'application ne se contente pas de lire la cellule "Total" d'Excel (souvent erronée car liée à des fichiers externes absents). 
**Elle recalcule manuellement la somme des colonnes C à K** dans le code Java (`Calculateur.java`) pour garantir l'exactitude des chiffres affichés.

## 3. Guide de Maintenance
Si vous devez modifier les sources de données :
1. Consultez le `Documents_Gestion/journal_developpement.md` pour comprendre l'historique des étapes.
2. Utilisez les outils dans `outils_diagnostic/` (ex: `DiagnosticSejours.java`) pour valider les nouveaux numéros de lignes/colonnes dans Excel.
3. Recompilez avec `./build.ps1 run` ou via la commande `javac` standard.

## 4. Prérequis
- Java 8 ou supérieur.
- Bibliothèques Apache POI (incluses dans `lib/`).
