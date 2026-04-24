# Note Technique : Fiabilisation du Moteur de Calcul
**Objet : Résolution des anomalies de surestimation (Cas Ruelle Saint-Pierre)**

## 1. Contexte
Lors de la phase de test du Dashboard, le site "Ruelle Saint-Pierre" présentait un écart de +1004% entre le coût théorique et le coût réel. Une investigation a été menée pour déterminer si cette anomalie était d'origine technique (bâtiment) ou informatique (algorithme).

## 2. Analyse des Anomalies de Structure (Data Quality)
L'audit croisé entre le programme Java et le calcul manuel a révélé deux irrégularités dans le fichier Excel `CALC DEP.xlsx` :

### A. Redondance des en-têtes (Mirroring)
*   **Symptôme** : Les dates de périodes de facturation sont saisies dans deux colonnes adjacentes (ex: Col N et Col O).
*   **Conséquence** : L'algorithme détectait deux fois le motif "2025" et ajoutait deux fois le montant de la facture au cumul annuel.

### B. Inclusion des agrégats (The Total Trap)
*   **Symptôme** : Le tableau contient des colonnes de synthèse en fin de ligne (Totaux Annuels, Cumuls).
*   **Conséquence** : Ces agrégats contenant également le mot-clé "25", ils étaient interprétés comme des factures individuelles et ajoutés au total global, doublant ainsi le montant réel.

### C. Erreurs de saisie humaine (Typo & Missing Keywords)
*   **Symptôme** : Absence du mot-clé "du" dans certains en-têtes ou fautes de frappe critiques (ex: "205" au lieu de "2025").
*   **Conséquence** : L'algorithme de détection classique ignorait ces colonnes, entraînant une sous-estimation du coût réel (ex: 10 factures trouvées au lieu de 13).

## 3. Solutions Implémentées (Refactoring)
Pour garantir une fiabilité à 100%, l'algorithme d'extraction a été renforcé :

1.  **Dédoublonnage intelligent (HashSet)** : Utilisation d'une structure de données "Set" pour mémoriser chaque période traitée. Si un bloc identique apparaît plusieurs fois, il est automatiquement ignoré.
2.  **Détection "Universal Date"** : L'algorithme ne cherche plus de mots précis (comme "du"), mais des motifs de dates (présence du séparateur "/" et du mot "au"). Cela permet de capturer les données malgré les fautes de frappe (ex: "205").
3.  **Audit en temps réel** : Intégration de logs d'audit détaillés permettant de vérifier manuellement la correspondance entre les colonnes Excel et les calculs pour chaque site critique.

## 4. Conclusion du Diagnostic
Après correction, les montants affichés correspondent désormais aux calculs manuels et aux factures réelles. 
**Résultat :** L'outil est passé d'un état de "Scanner brut" à un état de "Logiciel d'audit intelligent", capable de corriger les erreurs de saisie humaine dans les fichiers sources.
