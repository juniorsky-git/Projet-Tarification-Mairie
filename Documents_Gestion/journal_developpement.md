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

---

## Etape 8 : Correction et Finalisation du Dashboard LOISIRS (13/04/2026 10h39)

L'objectif de cette etape etait de rendre fonctionnel le tableau de bord pour l'Accueil Loisirs, qui affichait precedemment 0,00 euros malgré la presence de donnees dans le fichier source.

### Analyse du Probleme
- **Diagnostic** : Les lignes 42 a 46 de l'onglet Simulation (CALC DEP.xlsx) contiennent les donnees de depenses reelles pour les segments MDJ, P'TIT PRINCE, CLGAV, CLJP1 et CLLMICH.
- **Blocage technique** : La colonne R (index 17) utilise des formules Excel (`SUM(...)`) et des formats numeriques pouvant contenir des virgules. L'ancien code ne recuperait pas la valeur calculee en cache et echouait au parsing des chaines non standard.

### Resolution et Ameliorations
- **Moteur de calcul** : Refonte de `getValeurNumerique` dans `Calculateur.java` pour :
    - Extraire le resultat numerique des cellules de type `FORMULA`.
    - Harmoniser les separateurs decimaux (virgule vers point) pour `Double.parseDouble`.
- **Identification des Segments** : Amelioration de la recherche par mots-cles pour capturer correctement "p'tit prince" (insensible a la casse).
- **Compatibilite Java 8** : Correction de `ConsoleUI.java` qui utilisait `String.repeat()` (Java 11+). Une methode utilitaire compatible Java 8 a ete implementee pour garantir le fonctionnement sur l'environnement client.

### Resultat Final
Le dashboard LOISIRS est desormais certifie avec les indicateurs suivants :
- **Depenses reelles totales** : 141 497,74 euros.
- **Repartition par segment** :
    - CLGAV : 33 278,77 euros.
    - CLLMICH : 39 645,38 euros.
    - CLJP1 : 4 746,38 euros.
    - MDJ : 43 325,17 euros.
    - P'TIT PRINCE : 20 502,04 euros.

Cette etape marque la fiabilisation du pilotage financier pour l'ensemble des poles enfance.

---

## Etape 9 : Refactorisation Universelle et Tracabilite des Donnees (13/04/2026 11h23)

Afin de garantir la maintenance a long terme du projet, une refonte structurelle a ete menee, accompagnee d'un audit de tracabilite des donnees sources.

### Refactorisation "Clean Code"
- **Unification de la Lecture Simulation** : Creation de la classe `SimulationData` pour lire l'onglet Simulation une seule fois au lieu de deux. Cela optimise les ressources et assure la coherence des donnees entre les effectifs et les recettes.
- **Calculateur Generique** : Remplacement des methodes specifiques par une methode universelle `calculerDepenses(antenne, service, inclusion, exclusions)`. Cette approche permet d'integrer de nouveaux poles (ex: Ados, Etudes) sans modifier le moteur de calcul.
- **Standardisation UI** : L'affichage des tableaux de bord a ete harmonise via une methode `printLine` assurant un alignement parfait du texte et des valeurs.

### Audit de Tracabilite (Verification au 13/04/2026)
L'outil de diagnostic `VerificateurValeurs.java` a ete developpe pour confirmer l'origine exacte de chaque chiffre affiche dans les tableaux de bord.

#### Pôle SCOLAIRE (Cantine)
| Indicateur | Onglet Excel | Coordonnees (Lignes/Colonnes) |
| :--- | :--- | :--- |
| **Effectifs** | `Simulation` (8) | Lignes 8 a 16 / Colonne **D** |
| **Tarifs unitaires** | `Simulation` (8) | Lignes 8 a 16 / Colonne **C** |
| **Depenses reelles** | `Depenses restau 2025` (0) | Filtre **OU** (RESTMICH ou 2-RE) sur Colonne **H** |

> [!IMPORTANT]
> **Note sur le filtrage SCOLAIRE** : L'utilisation d'une logique **OU** (Antenne OU Service) est indispensable pour capturer l'integralite des factures (37 lignes pour 626 861,31 €). Une logique **ET** restreindrait le perimetre a 12 lignes seulement, faussant le cout de revient du repas (0,39 € au lieu de 3,97 €).

#### Pôle LOISIRS (Accueil Loisirs)
Toutes les depenses reelles proviennent de la colonne **R** de l'onglet **Simulation** (index 8).
- **MDJ** : Ligne 42, Colonne R.
- **P'TIT PRINCE** : Ligne 43, Colonne R.
- **CLGAV** (Gavroche) : Ligne 44, Colonne R.
- **CLJP1** (Jean-Pierre) : Ligne 45, Colonne R.
- **CLLMICH** (L. Michel) : Ligne 46, Colonne R.

### Étape 10 : Audit de Robustesse et Sécurisation (13/04/2026 à 12h00)
- **Objectif** : Sécuriser l'application contre les pannes et les données corrompues.
- **Réalisations** :
    - Création de `LogService.java` : Journalisation des erreurs dans `logs/erreur.log`.
    - Optimisation de `Calculateur.java` : Détection automatique des onglets et gestion des ressources Excel.
    - Outil `AuditStressTest.java` : Simulation de fichiers manquants et de données invalides pour tester la stabilité.
- **Résultat** : L'application ne crash plus en cas d'erreur de lecture et trace précisément l'origine du problème.

### Étape 11 : Module Eau et Exportation Totale (13/04/2026 à 14h10)
- **Objectif** : Extraire et analyser les données de consommation d'eau (onglet 'Conso eau').
- **Réalisations** :
    - Extraction bi-semestrielle détaillée (Colonnes 7/8 et 17/18) : montants TTC et m3.
    - Exportation brute vers TXT (`ExportCompletSimulation`, `ExportCompletEau`) avec traitement des formules Excel pour récupérer les **valeurs calculées**.
    - Démo multi-langage : script Python `analyse_conso_eau.py` pour vérification rapide.
- **Traceabilité** : Les données du 1er semestre (Col 8) et 2nd semestre (Col 18) sont consolidées pour un total annuel par PDL.

### Résultat Final
L'application est maintenant certifiée "Production-Ready" avec une traçabilité documentée, une architecture universelle et un moteur d'audit de données robuste.

### Étape 12 : Intégration du pôle Séjours (Vacances)
- **Objectif** : Créer un dashboard pour les séjours de vacances (Curie, Brassens, Espace Ados).
- **Structure des Données** :
    - Localisation : Onglet "Simulation", lignes 89 à 96.
    - Destinations : Espace ADO, Curie, Bressens.
- **Logique d'Extraction** :
    - Calcul dynamique par séjour en sommant les colonnes C à K (Transport, Hébergement, Restauration).
    - Cette approche garantit l'exactitude des chiffres même si les liens vers des fichiers Excel externes ne sont pas accessibles.
- **Résultat** : Nouveau Dashboard [4] fonctionnel affichant un budget total de **107 127,71 €**, détaillé par destination.

---

## Etape 13 : Consultation Multi-Grilles et Parsing Dynamique (16/04/2026)

L'objectif était d'offrir une flexibilité totale dans la consultation des tarifs en permettant de charger des grilles historiques (2024) ou futures sans modifier le code source.

### Défis Techniques
- **Analyse de texte (QF)** : Les grilles "Standalone" (ex: 2024) stockent les plages de Quotient Familial (QF) sous forme de texte ("10 814€ à 13 100€"). Un extracteur par segments a été développé pour retrouver les bornes numériques malgré les symboles et les espaces.
- **Robustesse Monétaire** : Les cellules Excel contenant des symboles "€" ou des virgules sont désormais converties proprement en valeurs numériques traitables par le moteur Java.

### Améliorations de l'Interface (ConsoleUI)
- Ajout d'un menu de sélection de grille :
    1. **Grille 2025 Interne** : Basée sur les constantes codées en dur pour une rapidité maximale.
    2. **Grille Personnalisée** : Permet l'importation de n'importe quel fichier Excel structuré en colonnes de services (Repas, Loisirs, etc.).

### Résultats
Le module a été validé avec le fichier `Grille-tarifaire-2024-(1).xlsx`, confirmant l'identification correcte de la tranche C pour un QF de 12 000€ et l'affichage des prix exacts de l'époque.

---

## Etape 14 : Résolution Technique et Extraction Exhaustive (16/04/2026)

L'objectif était de rendre l'outil "Infatigable" face aux variations de format et de corriger les instabilités de l'environnement de build.

### 1. Problème de Classpath (NoClassDefFoundError)
- **Problème** : Lors du passage à `make`, l'application ne trouvait plus les librairies Apache POI, provoquant un crash immédiat au chargement des fichiers Excel.
- **Cause** : Le Makefile pointait vers des noms de JAR génériques (ex: `poi.jar`) alors que les fichiers réels incluent des numéros de version (ex: `poi-5.3.0.jar`).
- **Solution** : Synchronisation complète du Makefile avec les noms de fichiers exacts présents dans le dossier `lib/`.

### 2. Problème d'Extraction des Bornes (QF)
- **Problème** : Les textes comme "10 814€ à 13 100€" étaient mal lus à cause des espaces de milliers et du caractère accentué "à".
- **Solution** : Implémentation d'un algorithme de "segmentation par séparateur" :
    - Remplacement de "à", "-", "a" par un tube `|`.
    - Nettoyage strict de chaque segment pour ne garder que les chiffres.
    - Recombinaison en nombres flottants (ex: "10 814" devient `10814.0`).

### 3. Extraction Exhaustive des Services
- **Problème** : L'utilisateur souhaitait voir TOUTES les colonnes de la grille (8 services) et non pas seulement les 4 principaux.
- **Réalisation** : Extension du mapping dans `DonneesTarifs.java` pour inclure :
    - Périscolaire (Matin ET soir / Matin OU soir)
    - Études (Forfait / 1/2 forfait)
    - Tarif Post-Études.
- **Résultat** : Une vue complète à 360° des tarifs de la ville pour chaque simulation.

---

## Etape 15 : Diagnostic Avancé et Découverte des Colonnes Masquées (16/04/2026)

Lors de l'ajout des pôles Ados et Séjours, un défi technique majeur a été rencontré concernant la visibilité des données.

### 1. Problème : "L'Angle Mort" de l'Inspecteur
- **Symptôme** : L'outil de diagnostic `InspecteurExcel` ne renvoyait que 10 colonnes, occultant totalement les données Jeunesse, Ados et Séjours situées plus à droite de la feuille.
- **Cause** : Le code de l'inspecteur comportait une limite codée en dur (`Math.min(10, ...)`) et un chemin de fichier fixe, empêchant l'analyse de sources alternatives.

### 2. Solution : Refonte de l'Outillage de Diagnostic
- **Action** : Modification de `InspecteurExcel.java` pour accepter des arguments en ligne de commande et élargissement du scan à 30 colonnes.
- **Résultat** : Découverte des colonnes 10 à 19 contenant l'intégralité des nouveaux tarifs demandés (Séjours 5j/6j, Classe découverte, Ados avec/sans repas).

### 3. Leçon de Programmation Défensive
Cet incident a souligné l'importance de ne pas faire d'hypothèses sur la structure des fichiers Excel fournis par les services (souvent très larges) et de maintenir des outils de diagnostic flexibles.

---

## Etape 16 : Conception du Moteur de Parsing Universel (16/04/2026)

Pour garantir que l'application fonctionne sans intervention humaine dans les années à venir, j'ai conçu un algorithme de **détection dynamique des colonnes**.

### 1. Pourquoi cette approche ?
Auparavant, le programme lisait la colonne 3 pour le "Repas". Si la mairie ajoute une colonne "Assurance" en colonne 3 l'an prochain, le programme lirait l'assurance au lieu du repas. 
**L'approche universelle** consiste à "explorer et comprendre" les étiquettes du tableau comme le ferait un humain, par recherche sémantique.

### 2. L'Algorithme "Context-Aware Mapping"
L'algorithme repose sur trois phases techniques :

1.  **Synthèse de Contexte (Cross-Row Exploration)** : 
    *   L'algorithme explore les 5 premières lignes du fichier. 
    *   Il scanne toutes les cellules pour trouver des mots-clés. Cela permet de distinguer un "repas scolaire" d'un "repas ados" si les mots-clés sont combinés sur plusieurs lignes.
2.  **Signature Scoring (Mots-Clés)** : 
    *   Chaque service (Repas, Loisirs, Séjours...) possède une "signature" composée de mots-clés obligatoires.
    *   *SÉJOUR 5J* : La colonne doit contenir "sejour" ET "5".
    *   *ADOS VACANCES* : La colonne doit contenir "ados" ET "vacances".
3.  **Cartographie des Index (Mapping Map)** : 
    *   Une fois la signature détectée, l'index de la colonne est enregistré dans une table de correspondance (`Map<String, Integer>`).
    *   Le moteur de calcul utilise ensuite cette table pour extraire les données, peu importe l'emplacement réel de la colonne.

### 3. Exemple de Logique de Code (Squelette Java)
```java
// On crée un dictionnaire de correspondance
Map<String, Integer> mapping = new HashMap<>();

for (int c = 0; c < 30; c++) {
    String label = cell.toString().toLowerCase();
    
    if (label.contains("sejour") && label.contains("5")) 
        mapping.put("SEJOUR_5J", c);
    else if (label.contains("repas") && !label.contains("ados")) 
        mapping.put("REPAS", c);
}
```

### 4. Pérennité (Futur-Proof)
- **Adaptabilité** : Si une nouvelle année, une colonne est insérée au milieu, l'algorithme "re-scannera" les en-têtes et trouvera le nouvel emplacement.
- **Rétrocompatibilité** : Si la détection automatique échoue (ex: tableau sans en-tête), l'application bascule par sécurité sur la structure standard de 2024.
- **Maintenance Facilitée** : Pas de code à modifier chaque année, l'outil s'auto-adapte à la source de données Excel.

---

## Etape 17 : Débogage Critique et Fiabilisation de la Signature (16/04/2026 16h00)

Suite aux tests de validation sur la Tranche F (QF 4500), une anomalie critique a été détectée et corrigée. Cette étape illustre le passage d'une détection simple à une **intelligence contextuelle**.

### 1. Détection de l'Anomalie
- **Symptôme** : Pour un QF de 4 500 €, le programme affichait un tarif repas de **0,81 €** au lieu des **2,00 €** attendus.
- **Analyse de la donnée** : La valeur "0,81" a été tracée dans la grille Excel : elle ne correspondait pas au pôle Restauration, mais au pôle **ESPACE ADOS (1/2 journée sans repas)**.

### 2. Diagnostic Technique : Collision de Mots-Clés
- **Cause Racine** : L'algorithme de l'Etape 16 cherchait le mot-clé "repas" de manière isolée. Or, ce mot apparaît plusieurs fois dans le tableau (Cantine scolaire, Ados avec repas, Ados sans repas).
- **Conséquence** : Le premier "repas" trouvé (ou le dernier selon le sens de lecture) écrasait le précédent dans la table de correspondance (Mapping Map), provoquant un faux positif.

### 3. Solution : La Signature Hiérarchique (Concaténation)
Plutôt que de faire de grosses mathématiques, j'ai utilisé une **logique de filtrage par contexte parental**.

- **Ancien Code** : `si (cellule contient "repas") -> colonne = Repas`
- **Nouveau Code** : 
    1.  **Fusion des En-têtes** : On concatène les lignes 1 (Pôle), 2 (Service) et 3 (Précision).
    2.  **Signature Combinée** : 
        *   La colonne 2 devient : `"RESTAURATION REPAS"`
        *   La colonne 14 devient : `"ESPACE ADOS 1/2 JOURNÉE SANS REPAS"`
    3.  **Filtrage Restrictif** : Pour le service `REPAS SCOLAIRE`, on impose que la signature contienne `"repas"` MAIS qu'elle soit associée au mot `"restauration"` ou qu'elle ne contienne PAS le mot `"ados"`.

### 4. Code Implémenté (Extrait)
```java
// On fusionne 3 niveaux de titres pour avoir le contexte
String fullLabel = (headerPôle + headerService + headerPrecision).toLowerCase();

if (fullLabel.contains("repas")) {
    // Si c'est du repas mais dans le pole Ados, on l'exclut du repas scolaire
    if (fullLabel.contains("restauration") || !fullLabel.contains("ados")) {
         mapping.put(REPAS, columnIndex);
    }
}
```

### 5. Conclusion du Débogage
Cette correction rend l'outil encore plus "Universel". Il ne se contente pas de chercher des mots, il **comprend la hiérarchie du document Excel**. La vérification finale sur le QF 4500 a confirmé le retour au tarif correct de **2,00 €**.

---

## Etape 18 : Validation de Cas Pratiques et Blindage des Formats (16/04/2026 16h45)

L'ultime phase de fiabilisation s'est concentrée sur deux axes : intercepter les erreurs de saisie avant le plantage de l'application (crash), et valider l'assise mathématique et algorithmique sur 4 profils tarifaires de test.

### 1. Blindage de la Saisie (Parser)
Dans un tableur manipulé par des humains, les prix peuvent contenir : `12 500,00` (Espace insécable), `12,50 €` (Symbole monétaire) ou même du texte inattendu (`A déterminer`). 
Le moteur d'extraction `getValeurNumerique` a été intégralement repensé avec des expressions régulières et du filtrage pour s'autocorriger :
- Suppression systématique des espaces cachés et devises.
- Analyse contextuelle de la virgule décimale (différenciée de la notation Anglo-saxonne des milliers ou vice-versa).
- En cas de texte irrécupérable, l'algorithme génère un avertissement dans la console sans interrompre la lecture des autres colonnes.

### 2. Tests de Recette sur 4 profils Quotients Familiaux
Un script de validation (`TestCasReels.java`) a été créé pour simuler des entrées massives sur la grille V1 de 2024.
*   **Famille A (QF >= 18000)** -> Tranche A. *Anomalie corrigée (Collision EXT) : L'algorithme se bloquait sur la ligne "Extérieur" car celle-ci happait tous les hauts de plafond. La ligne EXT a été isolée virtuellement.*
*   **Famille B (QF 10000)** -> Tranche D correctement identifiée.
*   **Famille C (QF 4500)** -> Tranche F correctement identifiée, avec confirmation du tarif Repas à 2.00€.
*   **Famille D (QF 0)** -> Tranche G (Tarif solidaire minimal) correctement assignée.

**Bilan : L'outil de Tarification Dynamique est pleinement qualifié.** Le moteur peut ingérer l'Excel de l'année N, extraire les services sans indexation rigide, et surmonter des erreurs d'inattention lors du remplissage par l'administration.
