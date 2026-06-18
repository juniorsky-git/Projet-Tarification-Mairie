# ISSUE-49 : Correction de la sauvegarde du nombre d'enfants et suppression de la simulation

## Description du problème
- Lors de la sauvegarde de la saisie comptable pour le nombre d'enfants, une erreur système (code 500 : `DataIntegrityViolationException`) se produisait car la propriété `annee` n'était pas passée correctement depuis le frontend, empêchant ainsi la persistance de l'ensemble de la saisie pour l'année en cours. Par conséquent, lors du rechargement de la page, le coût de l'enfant et le nombre d'enfants disparaissaient des détails.
- La section « Simulation financière » dans l'interface a été jugée obsolète par les utilisateurs depuis la suppression de la sauvegarde de l'historique de l'application et n'avait plus d'utilité pratique.

## Résolution
1. **Frontend (`index.html`)** :
   - Mise à jour de la fonction JavaScript `saisieMettreAJourStatEnfants()` pour forcer l'ajout de l'année courante (`annee: saisie.anneeActive`) dans l'objet de ligne statistique "Nombre d'enfants".
   - Suppression totale des pages, cartes d'atterrissage, et fonctions JavaScript liées à la "Simulation financière". 
2. **Impact** :
   - Le bouton "Sauvegarder" enregistre de nouveau correctement toutes les modifications, y compris la mise à jour dynamique du nombre d'enfants dans les calculs de coûts unitaires du Dashboard.
   - L'interface utilisateur est allégée.

## État
Résolu et poussé.
