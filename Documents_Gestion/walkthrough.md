# Walkthrough : Guide Technique et d Exploitation

Ce document est le guide complet pour comprendre, exploiter et maintenir l outil de tarification municipale de la Ville de Crosne.

---

## 1. Lancement de l Application

L application se compile et se lance via le script PowerShell fourni :

    powershell -ExecutionPolicy Bypass -File ./build.ps1 run

Le menu principal propose quatre options :
- Dashboard SCOLAIRE : Tableau de bord de la restauration scolaire base sur l onglet Simulation.
- Dashboard LOISIRS : Affiche les depenses reelles du centre de loisirs (Gaveroche).
- Dashboard ADOS : Affiche les depenses reelles de l Espace Ados.
- Consulter un tarif individuel : Calcule le tarif d un usager a partir de la saisie de son Quotient Familial.

---

## 2. Architecture des Fichiers Sources

L application repose sur deux fichiers Excel principaux situes dans le dossier Donnees/Autres.

### CALC DEP.xlsx (9 onglets)
C est le fichier central. Il contient les exports Ciril de la comptabilite municipale et le tableau de simulation financiere.

- Onglet 0 (Depenses restau 2025) : Toutes les factures engagees sur l exercice 2025. Chaque ligne correspond a une ecriture comptable avec son antenne, son service et son montant TTC.
- Onglet 8 (Simulation) : Tableau synthetique construit par le pole financier. Il contient par tranche tarifaire le prix reel facture aux familles, le nombre d enfants inscrits et le cout de reference de la mairie (4,42 euros).

### Feuille_dataviz.xlsx
Ce fichier contient des statistiques d usage par service. Il a ete utilise dans les premieres versions de l outil pour les effectifs, mais l onglet Simulation de CALC DEP.xlsx est desormais la source privilegiee car il contient les donnees directement validees par le pole financier.

---

## 3. Fonctionnement du Moteur de Calcul (Calculateur.java)

### Lecture de l Onglet Simulation
La methode chargerEffectifsDepuisSimulation() parcourt l onglet Simulation de CALC DEP.xlsx et lit :
- Le code de tranche en colonne B pour les tranches A a G.
- Le code EXT en colonne A pour les exterieurs (cas particulier car la colonne B est vide sur cette ligne).
- Le nombre d enfants en colonne D.
La boucle s arrete quand la colonne A contient le mot Total.

La methode calculerRecettesDepuisSimulation() multiplie pour chaque tranche :
    Prix reel (colonne C) x Nombre enfants (colonne D) x 140 jours

### Filtrage des Depenses par Pole
La methode calculerDepensesPole() lit l onglet principal des depenses (onglet 0) et applique des filtres :
- Par antenne (colonne T) : RESTMICH pour le scolaire, RESTCA pour les Ados, RESTGAV pour les Loisirs.
- Par service (colonne S) : Code 2-RE pour le perimetre restauration.
- Par mot-cle dans le libelle (colonne D) : Inclusion obligatoire ou exclusion selon le pole traite.

Ces filtres combinees permettent d isoler les depenses de chaque service sans risque de doublon.

---

## 4. Indicateurs Produits par le Dashboard Scolaire

Le dashboard scolaire affiche simultanement :
- Les effectifs lus depuis l onglet Simulation.
- Le cout moyen de reference de la mairie (4,42 euros).
- Le cout reel constate calcule depuis les factures (3,97 euros).
- Les depenses de reference : Total repas x 4,42 euros.
- Les recettes theoriques reelles : Calculees depuis les prix et effectifs de l onglet Simulation.
- Le taux de couverture : Recettes theoriques divises par Depenses de reference.
- L ecart budgetaire : Difference entre recettes et depenses.

---

## 5. Structure du Code Source (src/)

- Main.java : Point d entree. Gere le menu et appelle les methodes d affichage de chaque pole.
- Calculateur.java : Moteur de calcul. Lit les fichiers Excel et calcule tous les indicateurs.
- DonneesTarifs.java : Grille tarifaire complete 2025 (tous services, toutes tranches).
- Tarif.java : Modele d une tranche tarifaire avec une Map de prix par service.
- TarificationService.java : Recherche la tranche correspondant a un Quotient Familial donne.
- ConsoleUI.java : Utilitaires d affichage (logo, separateurs, en-tetes).

---

## 6. Structure du Dossier outils_diagnostic/

Ce dossier contient les scripts utilises pendant la phase d investigation. Ils ne font pas partie de l application principale mais sont gardes pour permettre la verification et la maintenance.

- InventairePoles.java : Liste toutes les antennes presentes dans CALC DEP.xlsx.
- ScannerTotalDataviz.java : Repere les sections et titres dans Feuille_dataviz.xlsx.
- DetecteurAdos.java : Liste toutes les factures dont le libelle contient le mot ADOS.
- InspecteurCalcDep.java : Affiche le nom et le nombre de lignes de chaque onglet de CALC DEP.xlsx.
- InspecteurSimulation.java : Affiche les premieres lignes de l onglet Simulation.
- DebugSimulationCols.java : Affiche le type et la valeur de chaque cellule des lignes de donnees de l onglet Simulation. C est ce script qui a permis d identifier la particularite de la ligne EXT.
- AnalyseTotale.java : Liste les factures retenues pour le perimetre scolaire.
- TestScolarestTotal.java : Isole les factures du prestataire Scolarest.

---

## 7. Maintenance et Mise a Jour des Donnees

Pour mettre a jour les resultats en debut d exercice ou apres une modification tarifaire :

- Remplacer CALC DEP.xlsx par le nouvel export Ciril.
- Verifier que l onglet Simulation est bien present a l index 8. Si l ordre des onglets change, mettre a jour la constante ONGLET_SIMULATION dans Calculateur.java.
- Si les prix de la grille tarifaire changent, mettre a jour DonneesTarifs.java (pour la consultation individuelle) ET l onglet Simulation du fichier Excel (pour les calculs automatiques).
- Recompiler et relancer via build.ps1.

---

## 8. Points d Attention et Limites Connues

- Les depenses des poles Loisirs et Ados ne produisent pour l instant que les montants de depenses reelles. Les recettes theoriques ne peuvent pas etre calculees car l onglet Simulation ne contient de tableau detaille que pour la restauration scolaire.
- Le fichier CALC DEP.xlsx doit rester dans le dossier Donnees/Autres/ avec exactement ce nom pour que l application fonctionne.
- Le message d avertissement Log4j2 qui apparait au lancement est inoffensif. Il indique seulement qu aucune configuration de journalisation avancee n a ete fournie.
