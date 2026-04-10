# Walkthrough : Guide d'Utilisation de l'Outil de Tarification Municipale

Ce document est un guide complet pour l'exploitation et la maintenance de la plateforme de pilotage financier de la Ville de Crosne.

## 1. Fonctionnement du Dashboard Multi-Poles
L'application a evolue pour devenir un outil de synthese multi-services. Chaque pole (Scolaire, Loisirs, Ados) dispose de sa propre logique d'analyse.

### Navigation dans le Menu Principal
Lors du lancement de l'outil via le script build.ps1, l'utilisateur a acces a quatre options :
- Dashboard SCOLAIRE : Affiche le bilan de la restauration scolaire de Louise Michel.
- Dashboard LOISIRS : Analyse le centre de loisirs (Antenne Gaveriaux).
- Dashboard ADOS : Analyse specifiquement l'Espace Ados (Antenne RESTCA).
- Consulter un tarif : Calculateur de prix individuel base sur le Quotient Familial.

## 2. Architecture du Moteur de Calcul (Calculateur.java)

### Mecanismes de Filtrage Comptable
Le calculateur utilise des regles d'inclusion et d'exclusion pour isoler les depenses directes de chaque pole dans le fichier CALC DEP.xlsx :
- Pole Scolaire : Interrogation de l'antenne RESTMICH et du service 2-RE. Exclusion des libelles contenant "ADOS" ou "LOISIRS" pour eviter les doublons.
- Pole Ados : Interrogation de l'antenne RESTCA combinee a un filtre obligatoire sur le mot-cle "ADOS".
- Pole Loisirs : Interrogation de l'antenne RESTGAV et du service 2-RE.

### Gestion des Effectifs (Dataviz)
Le systeme scanne dynamiquement le fichier Feuille_dataviz.xlsx :
- Il identifie les tranches (A a G et EXT) sous chaque titre de section (Restauration, Loisirs, etc.).
- Il multiplie le nombre d'usagers par le tarif correspondant et par le facteur de temps (exemple : 140 jours pour la cantine).

## 3. Maintenance de la Grille Tarifaire (DonneesTarifs.java)
L'outil repose sur une structure de type Map permettant de gerer des dizaines de prix differents par tranche. Pour modifier un tarif (exemple : augmentation du prix du repas de 5.54 a 5.70), il suffit de modifier la valeur correspondante dans la methode chargerTarifsReference de la classe DonneesTarifs.java.

## 4. Standards de Developpement et "Clean Code"
Pour garantir la transmission du projet aux services techniques de la mairie, le code respecte les standards suivants :
- Absence totale d'operateurs ternaires pour une lisibilite maximale des conditions.
- Documentation Javadoc exhaustive pour chaque classe et methode.
- Respect de la norme d'indentation professionnelle (accolades sur lignes separees).
- Utilisation de la bibliotheque Apache POI v5.3.0 pour la lecture robuste des fichiers Excel.

## 5. Bilan des Resultats de l'Analyse
L'audit final effectue par l'outil sur les donnees 2025 montre un taux de couverture de 102,25% pour le pole scolaire, confirmant l'equilibre budgetaire de ce service sur son perimetre direct.
