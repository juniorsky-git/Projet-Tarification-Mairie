# Changelog

## [1.3.0] - 2026-04-14
### Ajoute
- Nouveau service **`PdfExportService`** permettant de générer un rapport financier complet et professionnel.
- Guide technique exhaustif : `PDF_EXPORT_GUIDE.md` détaillant chaque méthode de rendu.
- Installation et configuration des dépendances Apache PDFBox 2.0.31.
- Intégration de l'option d'exportation `[8]` dans le menu principal de l'application.
- Signature d'auteur mise à jour sur l'ensemble du projet : **Séri-khane YOLOU**.

## [1.3.0] - 2026-04-14
### Ajoute
- Nouveau service **`PdfExportService`** permettant de générer un rapport financier complet.
- Guide méthodologique exhaustif : `PDF_EXPORT_METHODOLOGY.md` détaillant les algorithmes de rendu.
- Système d'indicateurs visuels (codes couleurs dynamiques) pour les taux de couverture.
- Installation et configuration des dépendances Apache PDFBox 2.0.31.
- Intégration de l'option d'export PDF `[8]` dans le menu principal (`Main`).
- Refonte complète du style de code : 100% accolades, interdiction des ternaires.

## [1.2.0] - 2026-04-13
### AJOUTÉ
- Prise en charge complète de l'onglet **`syntheses charges`** comme source de vérité unique (Fichier CALC DEP (3).xlsx).
- Intégration du pôle **Accueil Périscolaire** dans le menu principal.
- Nouvelle structure `SyntheseGlobale` dans le Calculateur pour un chargement optimisé.
- Affichage détaillé des dépenses par nature comptable pour tous les pôles.

### MODIFIÉ
- Les calculs de recettes utilisent désormais les multiplicateurs validés :
    - Restauration : 140 jours.
    - Études et Périscolaire : 10 mois.
    - Loisirs, Ados, Séjours : Forfait annuel (x1).
- Harmonisation complète de l'interface utilisateur.
- Simplification de la maintenance (les changements dans l'Excel sont détectés automatiquement).

### SUPPRIMÉ
- Dépendance aux fichiers CSV pour les dépenses.
- Anciennes méthodes de lecture fragmentées et obsolètes.
