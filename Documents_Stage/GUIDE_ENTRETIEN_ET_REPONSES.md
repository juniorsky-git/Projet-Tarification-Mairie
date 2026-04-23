# 🎤 Guide d'Entretien : Questions-Réponses (Soutenance de Stage)

Ce document récapitule toutes les explications et réflexions menées lors du développement du Diagnostic des Fluides pour la Mairie de Crosne.

---

## 🏛️ 1. L'Analogie Métier (Le Restaurant)
**Question : Comment expliquer simplement la logique de ton programme ?**
*Réponse* : Imaginez un client au restaurant. 
- **La Commande** : C'est ce que le bâtiment a consommé (kWh).
- **Le Menu** : Ce sont les prix officiels (ex: 0.31€/kWh).
- **Le Coût Théorique** : C'est le calcul mental du client pour savoir combien il va payer.
- **La Facture Réelle** : C'est l'addition que le serveur (le fournisseur) apporte.
- **Le Diagnostic** : On compare les deux. Si l'addition est 10 fois plus chère que le menu, on détecte une erreur de facturation.

## 📊 2. Origine des Nombres (Traçabilité)
**Question : D'où viennent les chiffres qui s'affichent dans ton tableau ?**
*Réponse* : Tout vient du fichier Excel `CALC DEP(4).xlsx`. Mon programme utilise un "Scanner" :
1. Il trouve la période (ex: "du 01/01/25 au...").
2. Il récupère la **Consommation** 2 colonnes plus loin.
3. Il récupère le **Montant TTC** 3 colonnes plus loin.
Les prix unitaires (0.31, 0.11, 2.10) sont des constantes stockées dans le code Java, basées sur les tarifs réglementés.

## 💰 3. Sources des Prix et Contexte Économique
**Question : Sur quoi te bases-tu pour ces tarifs précis (0.31€, 1.21€, 4.50€) ?**
*Réponse* : Ces chiffres sont calculés pour refléter le coût **réel** décaissé par la Mairie :
- **0.31€ (Elec)** : Moyenne nationale incluant l'acheminement (TURPE) et l'abonnement pro.
- **1.21€ (Gaz)** : Conversion du prix de marché (0,11€/kWh) vers le mètre cube (1m³ ≈ 11kWh). Multiplier les m³ par 0,11 serait une erreur grave car l'unité dans l'Excel est le volume.
- **4.50€ (Eau)** : Tarif incluant l'assainissement et le traitement des eaux usées (souvent oublié dans les calculs simples).

## 🧮 4. La Formule de l'Écart et le "Point de Référence"
**Question : Pourquoi la formule (Réel - Théorique) / Théorique ? Pourquoi diviser ?**
*Réponse* : Le **Coût Théorique** est notre "Point de Référence" (notre vérité idéale). On divise par lui pour obtenir un **pourcentage**. 
Le pourcentage permet de mesurer la **gravité** d'une erreur. 10€ d'erreur sur une petite facture est grave (10%), mais sur une énorme facture, c'est négligeable (0.1%). On divise pour pouvoir comparer tous les bâtiments entre eux de façon équitable.

## ⚡ 11. Le Mystère du Prix de l'Électricité (HP/HC et Taxes)
**Question : Pourquoi utiliser 0,31€ si le prix HT est de 0,16€ ?**
*Réponse* : Le prix de l'électricité est un empilement complexe. Pour une collectivité, mon chiffre de **0,31€ TTC** est une estimation "prudente" et complète :
1.  **HP/HC (Tarif Différencié)** : L'électricité est plus chère en journée (Heures Pleines : 0,166€ HT) que la nuit (Heures Creuses : 0,129€ HT). Comme les bâtiments publics consomment surtout le jour, le coût est naturellement plus haut.
2.  **Le passage du HT au TTC** : Il faut ajouter l'**Accise** (taxe sur l'énergie rétablie en 2025) et la **TVA à 20%**. 
3.  **L'Abonnement** : Les mairies ont des abonnements professionnels coûteux. Quand on divise le prix de cet abonnement par le nombre de kWh consommés, le prix "réel" du kWh monte significativement.
*Conclusion* : Utiliser 0,31€ permet de couvrir l'ensemble de ces frais invisibles dans un diagnostic global.

## 💧 12. Pourquoi 4,50€ pour l'eau ? (L'analogie de la vaisselle)
**Question : Pourquoi votre prix de l'eau est-il le double de celui du robinet ?**
*Réponse* : On utilise **l'analogie du restaurant** : 
- **L'Arrivée (2,10€)** : C'est le prix des ingrédients (eau potable qui arrive au robinet).
- **La Vaisselle Sale (Assainissement)** : C'est le prix du service pour nettoyer la table et les assiettes après le repas. 
En collectivité, on ne paye pas que l'eau que l'on reçoit, on paye aussi pour qu'elle soit **traitée et nettoyée** (assainissement) avant d'être rejetée dans la nature. Le prix de 4,50€ inclut ce cycle complet.
*C'est la différence entre le prix de "l'eau potable" et le prix du "service de l'eau".*

## 🎯 13. Analyse Stratégique : Les 3 Zones de Diagnostic
Pour l'oral, apprenez à lire le Dashboard par "Zones" :

1.  **ZONE DES ÉCONOMIES (Écarts Négatifs -20% à -30%)** :
    - *Signification* : La Mairie paye moins cher que mon estimation prudente de 0,31€.
    - *Verdict* : Excellente gestion contractuelle. Les sites sont performants.
2.  **ZONE D'ÉQUILIBRE (Écarts proches de 0% à 5%)** :
    - *Signification* : Le théorique et le réel sont alignés.
    - *Verdict* : Mon modèle de calcul est fiable et réaliste.
3.  **ZONE D'ALERTE (Écarts explosifs +1000%)** :
    - *Signification* : Le montant payé est 10 fois supérieur à la consommation réelle.
    - *Verdict* : **Erreur critique détectée**. Nécessite l'ouverture d'une "Issue" de maintenance immédiate.

## 🚨 5. Analyse de l'Anomalie (Ruelle Saint-Pierre)
**Question : Peux-tu nous détailler l'exemple de la Ruelle Saint-Pierre à +1004% ?**
*Réponse* : 
1. **Théorique** : 563,84 kWh x 0,31€ = **174,79€**.
2. **Réel** : Payé **1 929,89€** selon l'Excel.
3. **Écart** : (1929,89 - 174,79) / 174,79 = **x10**.
*Conclusion* : La Mairie a payé 10 fois trop cher. C'est une erreur de facturation flagrante détectée par mon outil.

## 📉 6. Interprétation des Résultats
- **Ecart Négatif (-20%)** : Économie réalisée par la commune.
- **Ecart Proche de 0** : Facturation saine et maîtrisée.
- **Ecart Positif Crititque (+50% et +)** : Erreur de facturation ou dysfonctionnement technique sur le bâtiment.

## ⚡ 7. Analyse Flash : La règle des 3 couleurs
Pour analyser le Dashboard en un coup d'œil, on utilise la segmentation suivante :

1.  **ZONE VERTE (0% à +/- 5%)** : *Gestion Parfaite*. Exemple : SERVICES TECHNIQUES (+0,7%). La Mairie paye le juste prix.
2.  **ZONE ORANGE (-10% à -30%)** : *Économie Budgétaire*. Exemple : GROUPE SCOLAIRE EUROPE (-20,9%). Les tarifs négociés sont meilleurs que le marché.
3.  **ZONE ROUGE (+50% à +1000%)** : *Alerte Critique*. Exemple : RUELLE SAINT-PIERRE (+1004%). Anomalie majeure qui nécessite une intervention immédiate pour arrêter la perte financière.

## ⚙️ 8. Fonctionnement Technique du "Scanner" (Détails)
**Question : Comment le programme fait-il pour ne pas se tromper de colonne ?**
*Réponse* : Le programme utilise une boucle qui vérifie chaque cellule une par une. 
- Dès qu'il détecte le mot "du", il sait qu'il est au début d'un bloc de facture. 
- Il capture la consommation à `colonne + 2` et le montant à `colonne + 3`.
- **Astuce technique** : Une fois la capture faite, il fait un "bond" de 3 colonnes en avant (`col += 3`) pour ne pas ré-analyser inutilement les données qu'il vient de lire. Cela garantit la rapidité et la fiabilité du traitement.

## 📚 10. Sources Officielles et Bibliographie
Pour justifier la rigueur des calculs, les sources suivantes ont été utilisées :

1.  **Électricité** : Commission de Régulation de l'Énergie (CRE) - *Observatoire des tarifs réglementés de vente (TRV) professionnels*. Base : 0,31€ / kWh (Tout compris).
2.  **Gaz** : Prix Repère de Vente de Gaz Naturel de la CRE (0,11€ / kWh converti à **1,21€ / m³** pour correspondre aux index Excel).
3.  **Eau** : Observatoire National des Services d'Eau et d'Assainissement (SISPEA). Moyenne intégrant l'assainissement à **4,50€ / m³**.
4.  **Municipal** : Délibérations du Conseil Municipal relatives aux tarifs des fluides et contrats de groupement de commandes énergétiques.
