package fr.mairie.tarification_api;

/**
 * DTO pour le transport des données d'analyse fluide vers le frontend.
 */
public record AnalytiqueFluide(
    String site,
    String fluide,
    double consommation,
    String unite,
    double montantReel,
    double montantTheorique,
    double ecart,
    double pourcentageEcart,
    boolean alerte,
    String periode
) {}
