# Walkthrough : Automatisation des Calculs de Tarification

J'ai finalisé l'automatisation de l'extraction des données financières. L'application ne se contente plus de lire des tableaux déjà faits, elle **calcule elle-même** les indicateurs à partir des fichiers sources de la mairie.

## 🚀 Fonctionnalités implémentées

### 1. Moteur de calcul (`Calculateur.java`)
Une nouvelle classe capable de croiser plusieurs sources de données :
- **Dépenses** : Extraction automatique depuis `CALC DEP.xlsx` (723 264 € trouvés).
- **Usagers** : Extraction depuis `Feuille_dataviz .xlsx` (1117 enfants identifiés).
- **Logique métier** : Application du coefficient de **140 jours** (scolarité annuelle).

### 2. Synthèse Dynamique dans le Main
Le point d'entrée affiche désormais un tableau récapitulatif clair avant de passer au simulateur de QF.

## 📈 Validation des résultats
Les calculs générés par Java concordent avec les fichiers Excel manuels de la mairie :
- **Coût moyen calculé** : 4,63 €
- **Taux de couverture** : 87,35 %
