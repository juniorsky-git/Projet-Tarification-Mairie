# Guide Technique : Système d'Exportation PDF (v1.2)

Ce document détaille de manière exhaustive l'architecture et la logique d'implémentation du service d'exportation PDF conçu pour la mairie de Crosne.

**Auteur :** Séri-khane YOLOU
**Date :** 14 Avril 2026
**Librairie :** Apache PDFBox 2.0.31

---

## 1. Architecture Globale

Le système repose sur un moteur de dessin de bas niveau (`PDPageContentStream`) qui transforme les données brutes issues du `Calculateur` en coordonnées graphiques. 

### Flux de données :
1. **Extraction** : Le `PdfExportService` interroge le `Calculateur` pour obtenir les dépenses totales et détaillées par pôle.
2. **Calcul** : Application des multiplicateurs (140j pour la cantine, 10 mois pour le périscolaire/études).
3. **Rendu** : Dessin séquentiel des pages (Garde -> 6 Pôles -> Synthèse -> Grilles).

---

## 2. Détail des Méthodes Clés

### `genererRapport(Calculateur calc, List<Tarif> grille)`
C'est le chef d'orchestre du service.
- **Logique** : Initialise le document `PDDocument`, crée le dossier de sortie `rapports/`, et appelle successivement les méthodes de dessin pour chaque section.
- **Sécurité** : Utilise un bloc `try-with-resources` pour garantir la fermeture du document et libérer la mémoire en cas d'erreur.

### `dessinerSectionPole(PDDocument doc, String pole, double multi, Calculateur calc)`
Génère une page dédiée pour l'un des 6 pôles municipaux.
- **Traitement des données** : Récupère la `Map<String, Double>` des charges détaillées.
- **Indicateurs** : Calcule le taux de couverture en temps réel : `(Recettes / Dépenses) * 100`.
- **Rendu Visuel** : Appelle `dessinerTableauDepenses` et `dessinerIndicateurs`.

### `dessinerTableauDepenses(PDPageContentStream cs, Map<String, Double> details, double total)`
Gère le rendu visuel des grilles de charges.
- **Alternance de couleurs** : Utilise une variable booléenne `alternate` pour colorer une ligne sur deux en gris clair (`GRIS_CLAIR`), améliorant la lisibilité.
- **Tronquage** : Utilise la méthode `tronquerTexte` pour éviter que les libellés de charges trop longs ne dépassent du tableau.

---

## 3. Logique de Tarification et Multiplicateurs

Un point crucial du système est l'application des volumes annuels validés :
- **Restauration** : Les tarifs unitaires sont multipliés par 140 (jours de fonctionnement estimés).
- **Scolaire/Périscolaire** : Multipliés par 10 (nombre de mois de service).
- **Loisirs/Ados** : Multipliés par 1 (considérés comme un forfait annuel global par enfant).

---

## 4. Design et Esthétique (Design System)

Pour garantir un rendu "Premium", le service utilise un système de couleurs et de polices strict :
- **Bleu Institutionnel** : `Color(30, 80, 150)` utilisé pour les bandeaux d'en-tête.
- **Feedback Visuel** : Le taux de couverture change de couleur selon sa valeur :
    - **Vert** : ≥ 80% (Équilibre satisfaisant)
    - **Orange** : ≥ 50% (Co-financement modéré)
    - **Rouge** : < 50% (Subvention municipale majoritaire)
- **Barre de progression** : Une barre graphique calculée dynamiquement illustre visuellement ce taux en bas de chaque page de pôle.

---

## 5. Maintenance et Évolutivité

Pour ajouter un nouveau pôle, il suffit de modifier la constante `POLES_CONFIG`. Le système s'adaptera automatiquement :
- Ajout automatique d'une nouvelle page.
- Calcul automatique des recettes via le `Calculateur`.
- Intégration automatique dans le tableau de synthèse global.
