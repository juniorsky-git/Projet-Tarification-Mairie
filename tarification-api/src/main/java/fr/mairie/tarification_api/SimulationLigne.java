package fr.mairie.tarification_api;

/**
 * Représente une ligne de l'onglet "Simulation" du fichier CALC DEP(4).xlsx.
 *
 * Chaque ligne correspond à une tranche tarifaire et contient les données
 * financières clés nécessaires à l'analyse budgétaire de la restauration.
 *
 * @author Séri-khane YOLOU
 * @version 1.0
 */
public class SimulationLigne {

    /** Libellé complet de la tranche (ex: "Tranche A"). */
    public String tranche;

    /** Code court de la tranche (ex: "A", "B", "G"...). */
    public String codeTranche;

    /** Prix facturé à l'usager pour cette tranche (en euros). */
    public double prixFacture;

    /** Nombre d'enfants concernés par cette tranche. */
    public double nombreEnfants;

    /** Coût moyen réel du service par enfant (en euros). */
    public double coutMoyen;

    /** Dépense annuelle totale générée par cette tranche (en euros). */
    public double depenseAnnuelle;

    /** Recette annuelle encaissée pour cette tranche (en euros). */
    public double recetteAnnuelle;

    /** Écart entre recette et dépense annuelles (en euros). */
    public double ecart;

    /** Taux de couverture des dépenses par les recettes (en %). */
    public double tauxCouverture;
}
