# Compte Rendu des Calculs Financiers : Evolution des Indicateurs

Ce document detaille l evolution chronologique des calculs effectues pour chaque service de la mairie de Crosne. Chaque phase correspond a une montee en precision des donnees et de la methode.

---

## Phase 1 : Premiere Estimation du Cout de Revient (Scolaire)

Le premier calcul s est concentre sur les factures de restauration comptabilisees dans Ciril.

- Source : Onglet Depenses restau 2025 de CALC DEP.xlsx.
- Perimetre : Antenne RESTMICH, service 2-RE.
- Total des depenses TTC retenues : 626 861,31 euros.
- Nombre de repas estime : Effectifs x 140 jours.
- Resultat initial : 3,97 euros par repas.
- Limite de cette phase : Ne couvre que les factures directes (prestataire Scolarest + denrees). Les charges communales (personnel, fluides) ne sont pas incluses.

---

## Phase 2 : Integration du Cout de Reference de la Mairie

Comparaison avec l audit interne produit par le pole financier municipal.

- Cible municipale : 4,42 euros par repas (perimetre complet : prestation + personnel + fluides + entretien).
- Ecart avec le calcul informatique : 0,45 euro par repas.
- Analyse de cet ecart : La difference est attribuable aux charges indirectes que Ciril ne capture pas directement dans l export utilise (masse salariale des agents, electricite, eau, gaz).
- Choix de conception : L outil affiche les deux chiffres simultanement. Les 4,42 euros servent de base pour calculer le taux de couverture. Les 3,97 euros restent affichés comme indicateur d audit comptable.

---

## Phase 3 : Harmonisation des Effectifs (Correction Tranche EXT)

Correction de l algorithme de comptage des enfants.

- Effectifs initiaux detectes : 1058 enfants.
- Cause de l erreur : La tranche EXT (exterieurs, QF superieur a 18 000 euros) n etait pas incluse dans le parcours du fichier source.
- Effectifs corriges : 1128 enfants.
- Impact : Les recettes theoriques ont ete revues a la hausse car 70 enfants de tranche EXT (tarif le plus eleve) n etaient pas comptes.

---

## Phase 4 : Expansion Multi-Services (Ados, Loisirs, Periscolaire)

L outil a ete generalise pour traiter plusieurs poles municipaux.

- Nouveaux poles integres : Espace Ados (antenne RESTCA), Centre de Loisirs (antenne RESTGAV).
- Methode : Filtrage par antenne ET par mot-cle dans le libelle de facture pour eviter les doublons entre services.
- Limite identifiee pour les autres poles : Le fichier source ne contient pas de tableau d effectifs par tranche pour les Ados et les Loisirs. Seules les depenses reelles peuvent etre affichees.

---

## Phase 5 : Audit de Fiabilite et Transparence des Calculs

Pour valider les chiffres produits, une batterie de scripts de diagnostic a ete developpee.

- AnalyseTotale.java : Liste toutes les factures retenues pour le perimetre Scolaire avec leur montant.
- TestScolarestTotal.java : Isole la part du prestataire Scolarest dans le total des depenses.
- DebugFullVolume.java : Parcourt l integralite du fichier de statistiques pour verifier la structure.
- InspecteurCalcDep.java : Identifie les 9 onglets presents dans CALC DEP.xlsx.
- DetecteurAdos.java : Confirme que les factures Espace Ados sont dans l antenne RESTCA.

Ces outils permettent a la mairie de verifier elle-meme le perimetre de chaque calcul et renforcer la confiance dans les indicateurs produits.

---

## Phase 6 : Recalibrage sur l Onglet Simulation (Indicateurs Definitifs)

Correction fondamentale apres identification de la veritable source de donnees utilisee par le pole financier.

### Explication de l Erreur des Phases Precedentes
Les recettes theoriques calculees precedemment (640 963,40 euros, taux de 102,25%) etaient basees sur les prix de la grille theorique stockee dans DonneesTarifs.java. Ces prix sont ceux definis dans le document PDF officiel de la mairie (grille de reference 2025), mais ils ne correspondent pas aux montants effectivement factures aux familles en 2025.

L onglet Simulation de CALC DEP.xlsx, construit directement par le pole financier, contient les prix reels pratiques par tranche. Pour exemple :
- Tranche A : 5,13 euros factures (vs 5,54 euros dans la grille theorique).
- Tranche G : 1,32 euros factures (vs 1,43 euros dans la grille theorique).

Ces ecarts entre grille theorique et facturation reelle s expliquent par des remises specifiques negociees avec la CAF et des ajustements operes en debut d exercice.

### Methode de Lecture de l Onglet Simulation
L onglet Simulation possede une particularite structurelle importante :
- La ligne EXT a son code directement en colonne A (index 0) avec la colonne B (index 1) vide.
- Toutes les autres tranches (A a G) ont leur code en colonne B, avec un libelle verbeux en colonne A.
- Le tableau se termine quand la colonne A contient le mot Total.

L algorithme a ete adapte pour gerer ces deux cas de figure dans une seule boucle.

### Indicateurs Definitifs
Ces chiffres sont les valeurs definitives produites apres correction complete :

| Indicateur | Valeur | Source |
| :--- | :--- | :--- |
| Effectifs totaux | 1128 enfants | Colonne D, onglet Simulation |
| Total repas annuels | 157920 repas | 1128 x 140 jours |
| Cout moyen de reference | 4,42 euros | Colonne E, onglet Simulation |
| Cout reel constate | 3,97 euros | Depenses TTC / Total repas |
| Depenses de reference | 698 006,40 euros | 157920 x 4,42 |
| Recettes theoriques reelles | 593 380,20 euros | Somme (Prix col C x Nb col D x 140) |
| Taux de couverture | 85,01 % | Recettes / Depenses reference |
| Ecart budgetaire | -104 626,20 euros | Recettes - Depenses reference |

### Interpretation
Un taux de couverture de 85,01% signifie que les tarifs actuellement factures aux familles couvrent 85% du cout complet de la restauration base sur la reference de la mairie. Les 15% restants representent la contribution nette de la commune pour maintenir ce service public, soit environ 104 626 euros sur l exercice 2025.

Cette analyse constitue une base solide pour decider si une revalorisation tarifaire est necessaire et dans quelle ampleur elle devrait s operer pour ameliorer le taux de couverture.
