# ISSUE-50 : Remplacement des Rapports Maîtres par un Guide / Tutoriel Intéractif

## Description du problème
- L'ancienne fonctionnalité "Rapports Maîtres" n'est plus pertinente suite à l'évolution de l'application et la suppression de l'historique complet.
- L'outil de tarification dispose de plusieurs fonctionnalités destinées aux agents administratifs (consultation, simulation, diagnostic, etc.) mais il manque une interface d'onboarding ("Tutoriel") permettant à tout nouvel utilisateur (voire même un bébé) de comprendre instantanément l'utilité de chaque module.
- Le document officiel de passation (`DOCUMENT PASSATION - OUTIL DE TARIFICATION 2026.pdf`) doit être rendu accessible d'un seul clic depuis l'interface de l'application.

## Résolution proposée
1. **Intégration du Document PDF** :
   - Déplacement du PDF dans les ressources statiques (`src/main/resources/static/document_passation.pdf`).
2. **Refonte de l'Interface Utilisateur (`index.html`)** :
   - Suppression totale de l'ancienne page HTML "Rapports Maîtres".
   - Remplacement de l'entrée dans le menu de navigation par un nouveau bouton "Guide & Tutoriel" (icône de livre / graduation).
   - Création de la page `#page-tutoriel` :
     - Bouton de téléchargement proéminent pour le PDF de passation.
     - 5 fiches visuelles très simples décrivant chaque sous-partie de l'application (Tableau de Bord, Consultation, Impact Familles, Audit Énergétique, Paramètres). L'objectif est une ergonomie hyper intuitive et accessible (icônes larges, textes épurés, couleurs douces).

## Impact
Amélioration majeure de l'accessibilité de l'application (UX/UI). Tout agent prenant en main le logiciel saura instantanément quelle page utiliser pour son besoin quotidien. Le fichier de passation est sauvegardé directement dans l'outil web.
