# Journal de Developpement : Projet de Tarification Municipale

Ce document retrace la chronologie complete des travaux effectues pour la Ville de Crosne dans le cadre d un stage BUT 2 Informatique. Chaque etape represente un jalon dans la comprehension et l automatisation des finances municipales.

---

## Etape 1 : Analyse Initiale et Calcul du Cout de Revient Reel

L objectif premier etait d identifier le cout reel d un repas produit pour la commune en croisant les donnees comptables.

- Action : Filtrage de l export Ciril (CALC DEP.xlsx) sur le perimetre de la restauration scolaire, antenne RESTMICH, perimetre service 2-RE.
- Resultat constate : Un cout de revient de 3,97 euros par repas.
- Methode : Division du total des depenses TTC par le nombre total de repas annuels (effectifs x 140 jours de classe).
- Limite : Ce chiffre represente uniquement les factures directes payees au prestataire (Scolarest) et les matieres premieres. Il ne tient pas encore compte des charges indirectes de la commune (personnel, fluides, amortissements).

---

## Etape 2 : Confrontation avec les References de la Mairie

Comparaison entre le calcul informatique automatise et les donnees produites manuellement par le pole financier.

- Donnee de reference fournie : La mairie a etabli un cout moyen de reference de 4,42 euros par repas apres un audit complet.
- Perimetre de ce cout de reference : Il integre la masse salariale des agents communaux, les fluides (eau, electricite, gaz), l entretien des locaux et les provisions.
- Ecart identifie : 0,45 euro entre le cout reel comptable (3,97 euros) et le cout de reference complet (4,42 euros).
- Decision de conception : L outil utilise desormais 4,42 euros comme cible contractuelle pour le calcul du taux de couverture, tout en affichant les 3,97 euros comme cout reel constate pour l audit comptable direct.

---

## Etape 3 : Correction des Statistiques d Effectifs (Tranche EXT)

Lors de l analyse des fichiers de statistiques, un bug a ete identifie sur le volume total d usagers.

- Probleme initial : Le total affiche etait de 1058 enfants. La tranche EXT (usagers exterieurs a la commune dont le QF est superieur a 18 000 euros) n etait pas comptabilisee par l algorithme.
- Cause technique : Le filtre de lecture dans le fichier Excel s arretait avant d atteindre cette ligne car le code EXT n etait pas reconnu comme une tranche valide.
- Resolution : Elargissement du filtre de lecture pour inclure explicitement la tranche EXT.
- Resultat final : Volume corrige a 1128 enfants, garantissant la precision des recettes theoriques.

---

## Etape 4 : Refonte du Moteur de Calcul (Calculateur.java)

Creation d un moteur de filtrage intelligent pour lire et croiser plusieurs sources de donnees Excel.

### Gestion des Antennes de Facturation
Le code identifie chaque service via son code antenne dans les depenses :
- RESTMICH : Restauration scolaire, ecole Louise Michel.
- RESTGAV : Restauration, centre de loisirs Gaveroche.
- RESTCA : Restauration Espace Ados.
- CRCOLL : Periscolaire et etudes surveillees.

### Algorithme de Filtrage Dynamique
Pour chaque pole, le moteur applique des regles d inclusion et d exclusion par mot-cle dans les libelles de factures. Cela evite de comptabiliser deux fois les memes depenses quand deux services partagent une meme antenne dans Ciril.

---

## Etape 5 : Refonte Clean Code et Standardisation

A la demande du projet, le code source a ete integralement refondu selon des standards professionnels.

- Suppression des operateurs ternaires : Tout operateur symbolique (?) a ete remplace par des structures if/else explicites et lisibles.
- Documentation Javadoc : Ajout de commentaires normalises pour chaque classe et methode, expliquant la logique metier et les parametres.
- Aeration du code : Application des conventions de placement des accolades et des sauts de ligne entre les blocs logiques.
- Standardisation ASCII : Remplacement des caracteres accentues dans les chaines affichees pour eviter les problemes d encodage sur les terminaux Windows.

---

## Etape 6 : Expansion Multi-Poles et Grille Tarifaire Complete

L outil a depasse le cadre de la restauration scolaire pour devenir une plateforme de pilotage multi-services.

### Integration de la Grille Tarifaire Massive
Une grille de prix complete pour 2025 a ete fournie et integree dans DonneesTarifs.java. Elle couvre tous les services municipaux :
- Restauration scolaire (tarif REPAS).
- Accueil de loisirs (ACCUEIL_JOURNEE, ACCUEIL_DEMI_REPAS).
- Espace Ados (ADOS_VAC_JOURNEE_REPAS, ADOS_VAC_JOURNEE_SANS, ADOS_SORTIE_DEMI, ADOS_SORTIE_JOURNEE...).
- Periscolaire (PERISCOLAIRE_MATIN_SOIR, PERISCOLAIRE_MATIN_OU_SOIR).
- Etudes surveillees (ETUDES_FORFAIT_MENSUEL, ETUDES_DEMI_FORFAIT).

La structure de donnees a ete migree de champs fixes vers une Map dynamique (Map<String, Double>) dans la classe Tarif.java, permettant une recherche du prix par cle de service.

### Outillage de Diagnostic
Pour garantir la fiabilite des calculs, des scripts specifiques ont ete developpes dans le dossier outils_diagnostic :
- InventairePoles.java : Liste toutes les antennes presentes dans Ciril.
- ScannerTotalDataviz.java : Parcourt le fichier de statistiques pour reperer les sections.
- DetecteurAdos.java : Identifie les factures liees a l Espace Ados.
- InspecteurCalcDep.java : Liste les onglets du fichier CALC DEP.xlsx.
- InspecteurSimulation.java : Affiche le contenu de l onglet Simulation ligne par ligne.

---

## Etape 7 : Correction Fondamentale de la Source de Donnees (Onglet Simulation)

Identification d une erreur de conception majeure sur la provenance des effectifs et des prix utilises.

### Probleme Identifie
Jusqu a cette etape, le code lisait les effectifs par tranche depuis le fichier Feuille_dataviz.xlsx, et utilisait les prix de la grille theorique (DonneesTarifs.java). Ces deux sources ne correspondaient pas aux donnees reellement utilisees par la mairie en 2025.

### Source Correcte : Onglet Simulation de CALC DEP.xlsx
Le fichier CALC DEP.xlsx contient 9 onglets. L onglet numero 8 (index 8) est nomme Simulation. C est la veritable source de reference construite par le pole financier. Il contient :
- Colonne A (index 0) : Libelle de la tranche (ex : Plus de 18 000 euros). Pour la ligne EXT, le code est directement dans cette colonne.
- Colonne B (index 1) : Code court de la tranche (A, B, B2, C, D, E, F, F2, G).
- Colonne C (index 2) : Prix reel facture a cette tranche en 2025. Ces prix sont inferieurs a la grille theorique car ils refletent les tarifs negocies avec la CAF et appliques concretement aux familles.
- Colonne D (index 3) : Nombre reel d enfants inscrits dans cette tranche.
- Colonne E (index 4) : Cout moyen de reference de la mairie (4,42 euros, valeur fixe).

### Difficulte Technique Rencontree et Resolution
La ligne EXT a une structure differente des autres lignes :
- Son code EXT est en colonne A (index 0) alors que la colonne B (index 1) est vide.
- Pour toutes les autres tranches (A a G), le code court est en colonne B et la colonne A contient le libelle verbeux.

L algorithme a du etre adapte avec une logique double :
- Si la colonne A contient exactement EXT, on prend EXT comme code de tranche.
- Sinon, on prend le contenu de la colonne B comme code de tranche.
- On s arrete quand la colonne A contient Total.

---

## Bilan Final : Indicateurs Definitifs (Apres Etape 7)

Ces chiffres sont produits par l outil en lisant directement l onglet Simulation de CALC DEP.xlsx et les depenses reelles de l onglet Depenses restau 2025.

| Indicateur | Valeur |
| :--- | :--- |
| Effectifs totaux (1128 enfants) | valides par croisement des deux sources |
| Total repas annuels (140 jours) | 157 920 repas |
| Cout moyen de reference mairie | 4,42 euros par repas |
| Cout reel constate (Ciril) | 3,97 euros par repas |
| Depenses de reference (1128 x 140 x 4,42) | 698 006,40 euros |
| Recettes theoriques reelles (Simulation) | 593 380,20 euros |
| Taux de couverture definitif | 85,01 % |
| Ecart budgetaire (Recettes - Depenses ref.) | -104 626,20 euros |

### Interpretation du Taux de Couverture
Un taux de 85,01% signifie que les tarifs actuellement factures aux familles couvrent 85% du cout total (base 4,42 euros) de la restauration. Les 15% restants representent une contribution nette de la commune au service de restauration, ce qui est une information cle pour les decisions tarifaires futures.
