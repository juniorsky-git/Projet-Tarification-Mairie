package fr.mairie.tarification_api;

import java.util.Map;

/**
 * Objet de réponse du tableau de bord financier.
 *
 * Contient les indicateurs clés d'un pôle de service (restauration, loisirs...)
 * calculés à partir des données de l'onglet Simulation de CALC DEP(4).xlsx.
 *
 * @author Séri-khane YOLOU
 * @version 1.0
 */
public class DashboardResponse {

    /** Nom du pôle concerné (ex: "Restauration"). */
    public String pole;

    /** Nombre total d'enfants concernés par ce pôle. */
    public double nombreEnfants;

    /** Coût moyen réel par unité de service (ex: coût réel d'un repas en €). */
    public double coutMoyenReel;

    /** Somme des dépenses réelles annuelles toutes natures confondues (en euros). */
    public double depensesTotales;

    /** Somme des recettes annuelles (non calculé pour l'instant, vaut 0). */
    public double recettesTotales;

    /** Taux de couverture global (non calculé pour l'instant, vaut 0). */
    public double tauxCouverture;

    /** Multiplicateur de tarif (non calculé pour l'instant). */
    public String multiplicateur;

    /**
     * Détail des dépenses annuelles par tranche tarifaire.
     * Clé = code tranche (ex: "A"), valeur = dépense annuelle (en euros).
     */
    public Map<String, Double> detailsCharges;
}
