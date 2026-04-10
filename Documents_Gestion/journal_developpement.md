# Journal de Developpement : Projet de Tarification Municipale

Ce document retrace la chronologie des travaux effectues pour la Ville de Crosne. Chaque etape represente un jalon dans la comprehension des finances municipales.

## Etape 1 : Analyse Initiale et Calcul du Cout de Revient Reel
L'objectif premier etait d'identifier le cout reel d'un repas produit pour la commune.
- **Action** : Filtrage de la comptabilite (Ciril) sur le perimetre de la restauration scolaire (Michali).
- **Resultat Constat** : Un cout de revient de **3,97 euros** par repas.
- **Analyse** : Ce chiffre est base uniquement sur les factures directes payees au prestataire et les matieres premieres. Il ne prend pas encore en compte les charges indirectes lourdes.

## Etape 2 : Confrontation avec les References de la Mairie
Comparaison entre le calcul informatique et les donnees du pole financier.
- **Donnee de Reference** : La mairie a etabli un cout de reference de **4,42 euros** apres un audit complet incluant la masse salariale et les fluides.
- **Observation de l'Ecart** : Une difference de 0,45 euro par repas a ete identifiee. 
- **Decision** : L'outil a ete mis a jour pour utiliser les 4,42 euros comme cible contractuelle pour le calcul du taux de couverture, tout en gardant les 3,97 euros comme "Cout Reel Constate" pour l'audit.

## Etape 3 : Correction des Statistiques d'Effectifs (Dataviz)
Lors de l'analyse des fichiers de statistiques, un bug a ete identifie sur le volume d'usagers.
- **Probleme** : Le total initial etait de 1058 enfants car la tranche "EXT" (Exterieurs) n'etait pas comptabilisee par l'algorithme.
- **Resolution** : Elargissement du filtre de recherche Excel pour inclure la tranche EXT.
- **Resultat Final** : Volume corrige a **1 128 enfants**, garantissant la precision des recettes theoriques calcules (640 963,40 euros).

## Etape 4 : Refonte "Clean Code" et Optimisation Professionnelle
A la demande des services informatiques, le code source a ete integralement refondu.
- **Suppression des Ternaires** : Remplacement de tous les operateurs symboliques (?) par des structures if/else explicites pour une meilleure maintenance.
- **Documentation JavaDoc** : Ajout de commentaires normalises pour chaque methode.
- **Aeration du Code** : Application des normes professionnelles pour le placement des accolades et des sauts de ligne.

## Etape 5 : Expansion Multi-Poles et Grille Tarifaire Massive
L'outil a desormais depasse le cadre de la fresque scolaire pour devenir une plateforme globale.
- **Integration** : Ajout d'une grille tarifaire complete (Ados, Loisirs, Etudes) basee sur une Map dynamique permettant de gerer des dizaines de prix differents par tranche.
- **Filtrage Intelligent** : Creation de regles d'inclusion/exclusion par antenne (RESTMICH, RESTGAV, RESTCA) pour generer des rapports financiers par service independant.

## Etape 6 : Outillage de Diagnostic et Audit des Fichiers Excel
Pour garantir la fiabilite des calculs, une batterie d'outils de diagnostic a ete developpee en marge de l'application principale.
- **Isolation des Outils** : Creation du dossier `outils_diagnostic` contenant des scripts specifiques (AnalyseTotale, InspecteurSource, DetecteurAdos).
- **Audit de la Structure Excel** : Ces scripts ont permis de decouvrir que les fichiers Dataviz et Ciril n'avaient pas les memes identifiants (exemple : l'Espace Ados etait cache dans l'antenne RESTCA alors que le Scolaire etait dans RESTMICH).
- **Transparence** : Ces outils permettent a la mairie de verifier elle-meme comment les donnees sont extraites, ce qui renforce la confiance dans les chiffres du Dashboard.

## Bilan Final et Conclusion
L'analyse finale a revele un taux de couverture de 102,25% pour le pole scolaire (Michali). L'outil est desormais capable de s'adapter a n'importe quel nouveau service municipal des que les volumes d'usagers sont renseignes dans les fichiers sources.
