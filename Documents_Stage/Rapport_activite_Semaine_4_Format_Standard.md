# 📓 Rapport d’activité de stage : Semaine 4
**Période :** Du 27 Avril au 30 Avril 2026
**Stagiaire :** Séri-khane YOLOU (BUT 2 Informatique)
**Maître de stage :** Bruno CRAMPE (Directeur Général des Services)

---

## 🏗️ 1. Introduction et Objectifs de la Semaine

Cette quatrième semaine (écourtée par le 1er mai) avait un double objectif. Côté développement, il s'agissait de **sécuriser l'application** (base de données, authentification) et de lui donner un design "Premium" digne d'un logiciel commercial. Côté exploitation, j'ai assuré une mission d'**assistance à maîtrise d'ouvrage (AMOA)** pour la direction générale en gérant le tri automatisé des flux de communication.

---

## 🗓️ 2. Journal de Bord Déterminé

### 🟢 Lundi 27 Avril : Persistance et Audit Avancé
- **Débogage Critique (P1)** : Résolution du bug "d'amnésie" du simulateur What-If. Les simulations sont désormais persistées dans le navigateur via `localStorage` (en utilisant une clé stable `codeTranche` plutôt qu'un index numérique) pour éviter les pertes de données.
- **Audit Bi-semestriel** : Extension du diagnostic énergétique. L'outil compare désormais le Semestre 1 et le Semestre 2 pour l'Eau, le Gaz et l'Électricité afin de détecter des variations de consommation anormales en cours d'année.

### 🔵 Mardi 28 Avril : Refonte UX Premium et Landing Page
- **Design System** : Refonte totale de l'interface du Dashboard en adoptant un style "Minimaliste SaaS" (inspiré de standards comme Stripe/Linear).
- **Landing Page** : Création d'une page d'accueil moderne (Hero banner, statistiques, maquettes animées) pour présenter l'outil aux utilisateurs de la mairie avant leur connexion.

### 🟣 Mercredi 29 Avril : Socle de Sécurité et Données
- **Base de Données** : Migration technique vers **PostgreSQL** pour garantir un stockage robuste et centralisé des données métier, en préparation du déploiement multi-utilisateurs.
- **Spring Security** : Implémentation du framework de sécurité Java pour gérer l'authentification et interdire l'accès aux tableaux de bord financiers aux personnes non autorisées.

### 🟠 Jeudi 30 Avril : Support Direction et Optimisations
- **Administration Système (Support DGS)** : Avant le départ en congés de M. CRAMPE, mission de configuration avancée de sa messagerie Outlook. Création de règles de tri automatisé complexes basées sur les statuts d'expédition (À vs Cc).
- **Résolution d'Incident** : Gestion d'un conflit d'identité Microsoft ("Plusieurs destinataires correspondent à l'identité") empêchant la création de règles spécifiques pour les emails du Maire. Mise en place de conditions de filtrage renforcées.
- **Optimisation RAM** : Correction d'une fuite de mémoire lors de la lecture des fichiers de logs lourds sur le serveur.

---

## ⚙️ 3. Zoom Stratégique : La Polyvalence (Code + Support)

Mon intervention du Jeudi sur la messagerie du Directeur Général démontre une compétence essentielle en informatique d'entreprise : le **support utilisateur**.
Face au blocage technique d'Outlook sur l'adresse du Maire, je n'ai pas abandonné. J'ai analysé l'erreur système et mis en place une logique de contournement (des règles de mots-clés plus fortes et des filtres croisés "À/Cc") pour assurer la continuité du service et l'organisation de la direction pendant ses congés.

---

## 🎓 4. Compétences Mobilisées
- **Administration et Réseau** : Gestion avancée de clients de messagerie professionnels (Outlook/Exchange) et résolution de conflits d'annuaire.
- **Sécurité Applicative** : Implémentation de Spring Security (Sessions, Mots de passe) et connexion à une base de données relationnelle (PostgreSQL).
- **UI/UX Design** : Intégration d'une interface graphique haut de gamme (Glassmorphism, animations fluides, indicateurs d'état "pulse").

---

## 🏁 5. Conclusion
La Semaine 4 a prouvé ma capacité à mener de front la **finalisation d'un logiciel métier complexe** (sécurisé et prêt à être déployé) et des missions d'**assistance directe** auprès de la haute hiérarchie de la mairie. L'outil "Tarification" est désormais un véritable SaaS opérationnel.
