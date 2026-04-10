# Documentation Technique : Plateforme Multi-Poles Municipal

Cette documentation detaille la structure finale de l'application apres la refonte multi-poles. L'outil est desormais capable d'analyser separement le Scolaire, le Centre de Loisirs et l'Espace Ados.

## 1. Evolution de l'Architecture des Donnees

### Grille Tarifaire Massive (DonneesTarifs.java)
L'application n'utilise plus une liste simple de prix mais une structure de type Map pour chaque tranche (A a G + EXT). 
Cela permet de stocker pour chaque quotient familial :
- Le prix unitaire du repas scolaire.
- Les prix de l'accueil de loisirs (Journee, Demi-journee, avec ou sans repas).
- Les tarifs specifiques de l'Espace Ados (Vacances, Sorties).
- Les forfaits mensuels pour les etudes surveillees.

### Moteur de Calcul (Calculateur.java)
Le calculateur a ete generalise pour accepter des filtres dynamiques lors de l'analyse des depenses comptables (Ciril) :
- Filtrage par antenne : Utilisation des codes precis (RESTMICH pour Louise Michel, RESTGAV pour Gaveriaux, RESTCA pour les Ados).
- Filtrage par libelle : Inclusion ou exclusion de mots-cles dans le libelle de la facture (exemple : extraire uniquement les factures contenant "ADOS" sur l'antenne RESTCA).

## 2. Analyse des Resultats par Pole

### Pole Scolaire (Cantine)
- Antenne utilisee : RESTMICH.
- Service filtre : 2-RE.
- Exclusions : Factures contenant "ADOS" ou "LOISIRS".
- Resultat : Taux de couverture de 102,25%. Cela indique que les recettes theoriques generables par les 1128 enfants identifies couvrent integralement les depenses directes de la restauration scolaire.

### Pole Espace Ados
- Antenne utilisee : RESTCA.
- Filtre obligatoire : "ADOS".
- Resultat : Depenses identifiees de 9 301,34 euros. 
- Note sur les recettes : Le fichier de statistiques (Dataviz) ne contient pas encore la repartition des ados par tranche (A, B, C...). Par consequent, les recettes theoriques sont affichees a 0 en attendant l'ajout de ces donnees dans le fichier Excel source.

## 3. Guide d'Exploitation

### Compilation et Lancement
L'outil se compile via le script build.ps1. Le menu principal offre desormais un acces direct a chaque tableau de bord financier.

### Maintenance des Donnees
Pour mettre a jour les resultats :
- Depenses : Remplacer le fichier CALC DEP.xlsx par l'export le plus recent.
- Effectifs : Mettre a jour le fichier Feuille_dataviz.xlsx. L'outil detectera automatiquement les nouveaux volumes si les tranches (A, B, C...) sont renseignees sous les titres de sections correspondants.
