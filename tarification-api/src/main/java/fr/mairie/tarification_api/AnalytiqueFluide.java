package fr.mairie.tarification_api;

/**
 * DTO pour le diagnostic de performance d'un fluide par site.
 */
public record AnalytiqueFluide(
    String site,
    String fluide,        // "Eau", "Electricité", "Gaz"
    double consommation,  // m3 ou kWh
    String unite,        // "m3" ou "kWh"
    double montantReel,   // Montant facturé TTC
    double montantTheorique, // Montant calculé par la formule (Prix unitaire * Conso + Fixe)
    double ecart,         // Différence entre Réel et Théorique
    double pourcentageEcart, // Écart en %
    boolean alerte        // True si l'écart dépasse un seuil (ex: 20%)
) {}
