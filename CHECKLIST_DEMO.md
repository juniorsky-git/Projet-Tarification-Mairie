# Checklist Démo : Simulation Dynamique (Version 2.0)
**Objectif :** Démontrer la fiabilité et le blindage de l'outil devant le jury.

---

### 🟢 Scénario 1 : Le "Succès Master" (Chargement 2024)
*But : Montrer que l'outil est capable de voyager dans le temps.*

1.  Lancer le programme : `./build.ps1 run`
2.  Menu : Choisir **[7] Consulter un tarif individuel**
3.  Grille : Choisir **[2] Charger une autre grille**
4.  Chemin : Saisir `Donnees/Grille-tarifaire-2024-(1).xlsx`
5.  QF : Saisir `8500`
6.  **Résultat attendu** : "Tranche detectee : E" + affichage de tous les tarifs 2024.

---

### 🟠 Scénario 2 : Le "Blindage Absent" (Fichier imaginaire)
*But : Montrer que l'outil gère les erreurs humaines sans crasher.*

1.  Menu : Dans le menu [7]
2.  Grille : Choisir **[2]**
3.  Chemin : Saisir `Grille_2099.xlsx`
4.  **Résultat attendu** : Message d'erreur rouge `ERREUR : Le fichier specifie est introuvable.` + retour au menu propre.

---

### 🔴 Scénario 3 : Le "Blindage Structure" (Mauvais format)
*But : Montrer l'intelligence du moteur (vérification du contenu).*

1.  Menu : Dans le menu [7]
2.  Grille : Choisir **[2]**
3.  Chemin : Saisir `Donnees/tarifaire-grille.pdf`
4.  **Résultat attendu** : Message d'erreur rouge `ERREUR : Le fichier doit avoir l'extension Excel.` ou structure invalide.

---

### 🏆 Bonus Final : Export PDF
1.  Choisir **[8] Générer le rapport PDF complet**
2.  Montrer le fichier généré dans le dossier `/rapports`.

---
*Document de support pour l'entretien d'admission.*
