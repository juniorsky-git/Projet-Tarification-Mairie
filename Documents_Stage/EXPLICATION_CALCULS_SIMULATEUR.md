# Explication de la Méthode de Calcul du Simulateur d'Impact Tarifaire

Ce document détaille la logique de calcul implémentée dans le module **Simulateur d'Impact Tarifaire — Accueil de Loisirs** (onglet **Impact Familles** de l'interface).

---

## 1. Données de Référence

Le simulateur s'appuie sur deux éléments principaux déclarés dans l'interface (`index.html`) :
1. **La Grille Tarifaire 2025 (`AL_TARIFS_2025`)** : Les tarifs unitaires pour chaque prestation, ventilés par tranche de Quotient Familial (QF) de A à G.
2. **Le Volume Mensuel de Référence (`AL_PRESTATIONS`)** : Les fréquences mensuelles moyennes pour une famille type de chaque pôle d'activité.

### Tableau des Fréquences de Référence par Famille

| Groupe | Prestation | Clé Technique | Quantité / Mois |
| :--- | :--- | :--- | :---: |
| **Mercredis** | Journée avec repas (mercredi) | `journeeAvecRepas` | 4 |
| | 1/2 journée avec repas (mercredi) | `demiJourneeAvecRepas` | 4 |
| **Périscolaire** | Matin et soir | `matinEtSoir` | 16 |
| | Matin ou soir | `matinOuSoir` | 16 |
| **Post-études** | Post-études sans goûter | `postEtudes` | 16 |
| **5 jours consécutifs** | Journée avec repas (5 j) | `journeeAvecRepas5j` | 5 |
| | Journée sans repas (5 j) | `journeeSansRepas5j` | 5 |
| | 1/2 journée avec repas (5 j) | `demiJourneeAvecRepas5j` | 5 |
| | 1/2 journée sans repas (5 j) | `demiJourneeSansRepas5j` | 5 |
| **Sorties / Stages** | Sortie 1/2 journée IDF / Stage | `sortieDemi` | 1 |
| | Sortie journée / Stage | `sortieJournee` | 1 |
| **Séjours** | Séjour 5 jours | `sejour5j` | 1 |
| | Séjour 6 jours | `sejour6j` | 1 |

---

## 2. Formules Mathématiques Appliquées

Pour chaque prestation $p$ dans la liste :

### A. Facture de Base Mensuelle
Le coût de base est obtenu en multipliant le tarif unitaire de la tranche sélectionnée par la quantité mensuelle de référence :

$$\text{Facture de Base}_p = \text{Tarif Unitaire}_p \times \text{Quantité}_p$$

### B. Facture Simulée Mensuelle
Lorsqu'une hausse tarifaire globale $H$ (en %) est appliquée via le curseur de simulation, le tarif simulé unitaire est recalculé :

$$\text{Tarif Simulé}_p = \text{Tarif Unitaire}_p \times \left(1 + \frac{H}{100}\right)$$

La facture simulée de la prestation est donc :

$$\text{Facture Simulée}_p = \text{Tarif Simulé}_p \times \text{Quantité}_p = \text{Facture de Base}_p \times \left(1 + \frac{H}{100}\right)$$

### C. Surcoût par Prestation
Le surcoût mensuel pour une prestation donnée est la différence entre le coût simulé et le coût de base :

$$\text{Surcoût}_p = \text{Facture Simulée}_p - \text{Facture de Base}_p$$

---

## 3. Synthèse et Indicateurs Globaux (KPIs)

Les indicateurs de synthèse affichés en haut de l'écran consolident les résultats de toutes les prestations applicables :

*   **Facture de base totale / mois** :
    $$\text{Facture Base Totale} = \sum_{p} \text{Facture de Base}_p$$

*   **Facture simulée totale / mois** :
    $$\text{Facture Simulée Totale} = \sum_{p} \text{Facture Simulée}_p$$

*   **Surcoût mensuel total** :
    $$\text{Surcoût Mensuel} = \text{Facture Simulée Totale} - \text{Facture Base Totale}$$

*   **Surcoût annuel estimé** :
    $$\text{Surcoût Annuel} = \text{Surcoût Mensuel} \times 12\text{ mois}$$
    *(Note : Le coefficient de projection sur l'année scolaire de référence est de 12 mois).*

---

## 4. Exemple Concret (Tranche C - Hausse de 10%)

### Prestation : « Journée avec repas (mercredi) »
*   **Quantité de référence** : 4 jours / mois
*   **Tarif de base unitaire 2025** : 12,40 €
*   **Calcul de base** : $12,40\text{ €} \times 4 = \mathbf{49,60\text{ €}}$
*   **Calcul simulé (+10%)** : $49,60\text{ €} \times 1,10 = \mathbf{54,56\text{ €}}$
*   **Surcoût** : $54,56\text{ €} - 49,60\text{ €} = \mathbf{+4,96\text{ €}}$

### Consolidation Globale Mensuelle (Tranche C)
*   **Facture de base cumulée** : $\mathbf{478,43\text{ €}}$
*   **Facture simulée (+10%)** : $478,43\text{ €} \times 1,10 = \mathbf{526,27\text{ €}}$
*   **Surcoût mensuel** : $526,27\text{ €} - 478,43\text{ €} = \mathbf{+47,84\text{ €}}$
*   **Surcoût annuel estimé** : $47,84\text{ €} \times 12 = \mathbf{+574,12\text{ €}}$
