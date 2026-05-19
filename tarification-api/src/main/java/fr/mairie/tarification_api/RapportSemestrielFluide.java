package fr.mairie.tarification_api;

/**
 * DTO pour le rapport de consommation bi-semestriel des fluides.
 */
public record RapportSemestrielFluide(
    String site,
    String fluide,
    double m3_S1,
    double m3_S2,
    double m3_Total,
    double reel_S1,
    double reel_S2,
    double reel_Total,
    double delta_S1_S2_Percent, // Evolution entre S1 et S2
    boolean alerte,
    String remarque,
    String unite
) {}
