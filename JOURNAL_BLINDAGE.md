# Journal de Blindage : Workflow & Solutions Techniques
**Date :** 17 avril 2026 - 14:05

Ce document retrace le workflow de diagnostic et de résolution appliqué pour sécuriser le passage au mode dynamique (Version 2.0).

## 1. Diagnostic de l'Erreur (Le "Hole")
### Observation
L'interface utilisateur (`ConsoleUI.java`) possédait un bloc `try/catch` pour intercepter les erreurs de grille (`IllegalArgumentException`), mais la méthode de chargement (`DonneesTarifs.chargerGrilleStandard`) ne lançait aucune exception en cas de fichier mal formé. Elle se contentait d'échouer silencieusement ou de crasher plus tard.

### Conséquence
Si un agent chargeait un fichier Excel vide ou avec une mauvaise structure, l'outil plantait violemment au lieu d'afficher un message d'erreur propre.

## 2. Solutions Java Implémentées
Nous avons mis en place une stratégie de "Fail-Fast" (Échouer tôt) dans `DonneesTarifs.java` :

| Verrou (Guard) | Condition de rejet | Message d'erreur associé |
| :--- | :--- | :--- |
| **Garde Structurelle 1** | Nombre de feuilles = 0 | "Le fichier Excel est vide (aucune feuille detectee)." |
| **Garde Structurelle 2** | Nombre de lignes < 6 | "Structure invalide : moins de 6 lignes detectees." |
| **Garde de Contenu 3** | Entête absente/incomplète | "L'en-tete de colonnes est absente ou complète." |

## 3. Workflow de Résilience (Script de Build)
### Erreur rencontrée
Le script `./build.ps1` utilisait un chemin vers le JDK Java 21 codé en dur (`redhat.java-1.53.0`). Suite à une mise à jour mineure de l'environnement, le script est devenu inutilisable.

### Solution apportée
Refonte du script PowerShell pour utiliser l'auto-détection :
```powershell
$JavaExtension = Get-ChildItem $ExtensionsPath -Filter "redhat.java-*-win32-x64" | Sort-Object Name -Descending | Select-Object -First 1
```
*Bénéfice :* Le script fonctionnera désormais sur n'importe quel poste, même après une mise à jour de l'IDE.

## 4. Workflow de Vérification
Validation finale effectuée via 3 scénarios réels (Agent Mairie) :
1. **Scénario Nominal** : Chargement de la grille 2024 -> Succès.
2. **Scénario Intrusion** : Chargement d'un PDF -> Rejet propre (Extension).
3. **Scénario Manquant** : Saisie d'un nom de fichier erroné -> Rejet propre (Existence).

---
*Document consignant l'industrialisation de la sécurité du projet.*
