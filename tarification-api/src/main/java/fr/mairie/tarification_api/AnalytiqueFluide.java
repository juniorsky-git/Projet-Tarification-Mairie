package fr.mairie.tarification_api;

/**
 * Record représentant une ligne de diagnostic fluide consolidée.
 * 
 * Cette structure immuable contient les résultats de l'audit pour un bâtiment précis.
 * 
 * @param site Nom du bâtiment (ex: École Robert Desnos).
 * @param fluide Type d'énergie (Eau, Gaz, Électricité).
 * @param consommation Quantité brute consommée.
 * @param unite Unité de mesure (m3 ou kWh).
 * @param montantReel Somme des factures TTC extraites de l'Excel.
 * @param montantTheorique Coût calculé sur la base des tarifs municipaux 2025.
 * @param ecart Différence brute (Réel - Théorique).
 * @param pourcentageEcart Pourcentage d'écart (permet de juger la gravité).
 * @param alerte Indicateur visuel si l'écart dépasse le seuil critique de 20%.
 * @param periode Plage de dates couverte par l'audit (ex: du 01/01/25 au 15/12/25).
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
