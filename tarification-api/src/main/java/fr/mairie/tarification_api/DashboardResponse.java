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

    /** Somme des dépenses annuelles de toutes les tranches (en euros). */
    public double depensesTotales;

    /** Somme des recettes annuelles de toutes les tranches (en euros). */
    public double recettesTotales;

    /** Taux de couverture global : recettes / dépenses × 100 (en %). */
    public double tauxCouverture;

    /**
     * Multiplicateur de tarif calculé : coût moyen / prix facturé moyen.
     * Indique de combien les tarifs couvrent le coût réel.
     */
    public String multiplicateur;

    /**
     * Détail des dépenses annuelles par tranche tarifaire.
     * Clé = code tranche (ex: "A"), valeur = dépense annuelle (en euros).
     */
    public Map<String, Double> detailsCharges;
}
