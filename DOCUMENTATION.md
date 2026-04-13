# Documentation du projet Outil de Tarification Municipale

## 1. Contexte
Ce projet Java analyse les dépenses et recettes des services municipaux en utilisant les données publiées par le pôle financier.

## 2. Sources de données principales
- `Donnees/Autres/CALC DEP (3).csv` : source primaire des dépenses synthétisées.
- `Donnees/Autres/CALC DEP (3).xlsx` : source secondaire pour les détails et les feuilles de simulation.

## 3. Principales fonctionnalités
- Dashboard multi-pôles : Scolaire, Loisirs, Ados, Séjours, Études.
- Méthodes de calcul financière par pôle dans `src/fr/mairie/tarification/Calculateur.java`.
- Lecture directe des totaux de dépenses dans le CSV pour garantir des montants fiables.
- Rapport complet par pôle avec :
  - dépenses totales
  - résultat financier
  - coût moyen par enfant
  - ratio dépenses/recettes

## 4. Logique de calcul
### Dépenses
- Les montants totaux par pôle sont extraits du CSV via `Calculateur.lireTotauxDepensesDepuisCSV()`.
- Chaque pôle a une méthode dédiée :
  - `calculerDepensesRestauration()`
  - `calculerDepensesAccueilLoisirs()`
  - `calculerDepensesAccueilPeriscolaire()`
  - `calculerDepensesEtudesSurveillees()`
  - `calculerDepensesEspaceAdos()`
  - `calculerDepensesSejours()`
  - `calculerTotalDepensesGenerales()`

### Recettes
- Les recettes théoriques de la restauration sont calculées via `chargerDonneesSimulation()`.
- Les recettes des autres pôles sont préparées par des méthodes dédiées, prêtes à être remplies lorsque les données seront disponibles.

### Rapports
- `Calculateur.genererRapportCompletParPole()` construit un rapport textuel de synthèse.
- Le menu principal propose un accès direct à ce rapport.

## 5. Fichiers clés
- `src/fr/mairie/tarification/Calculateur.java` : moteur de calcul.
- `src/fr/mairie/tarification/Main.java` : entrée, menu et navigation.
- `src/fr/mairie/tarification/ConsoleUI.java` : affichage console.
- `Donnees/Autres/CALC DEP (3).csv` : dépenses synthétisées.
- `Donnees/Autres/CALC DEP (3).xlsx` : support détaillé des données.

## 6. Exécution
1. Compiler avec Java et Apache POI.
2. Lancer `fr.mairie.tarification.Main`.
3. Utiliser les menus pour accéder aux tableaux de bord et au rapport complet.

## 7. Maintenance
- Si les fichiers sources changent, mettez à jour les constantes de chemin dans `Calculateur.java`.
- Vérifiez les indices de colonne du CSV et de l'onglet `Simulation` dans `CALC DEP (3).xlsx`.
- Utilisez les outils dans `outils_diagnostic/` pour valider les nouvelles lignes.
