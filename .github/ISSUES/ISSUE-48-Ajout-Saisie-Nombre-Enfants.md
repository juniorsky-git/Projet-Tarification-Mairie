# [FEATURE] Ajout Saisie du Nombre d'Enfants et Nettoyage UI

## Description
Il était nécessaire de permettre à l'utilisateur de spécifier le nombre d'enfants par pôle lors de la saisie d'une nouvelle année comptable, afin que le tableau de bord puisse calculer et afficher un **Coût unitaire réel** exact.
Par ailleurs, les menus "Simulation financière" et "Historique", devenus obsolètes ou inutiles avec la nouvelle interface, devaient être retirés pour alléger la navigation.

## Tâches réalisées
- **UI / Frontend** : 
  - Suppression des liens "Simulation financière" et "Historique" dans la barre de navigation latérale.
  - Ajout d'un champ dynamique "Nombre d'enfants" au-dessus du tableau des dépenses dans la Saisie Comptable.
  - Enregistrement de cette donnée sous le type de ligne `STAT` lors de la sauvegarde.
- **Backend** : 
  - Modification de `DashboardController` pour récupérer la ligne `STAT` "Nombre d'enfants" et calculer dynamiquement le `coutUnitaire`.
  - Modification de `PolesController` pour propager cette même information dans l'API globale.

## Tâches à faire (Next Steps)
- Vérifier et valider le bon fonctionnement du calcul lors de l'export PDF ou Excel.
- Implémenter la suppression propre des vues HTML correspondantes (les balises `<div id="page-simulation">` et `<div id="page-historique">`) pour nettoyer définitivement le code source de `index.html`.

## Statut
**Terminé (Code pushé)**
