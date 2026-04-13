# Changelog

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
