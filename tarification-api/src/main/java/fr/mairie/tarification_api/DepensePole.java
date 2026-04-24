package fr.mairie.tarification_api;

import java.util.Map;

/**
 * Représente la synthèse budgétaire d'un pôle tarifaire.
 * 
 * NOTE TECHNIQUE (Usage du 'record') :
 * Contrairement à une 'class', un 'record' est un porteur de données immuable.
 * Analogie : C'est comme une "Carte d'Identité" ou un "Ticket de Caisse". 
 * Une fois créé, les données ne peuvent plus être modifiées, ce qui garantit 
 * l'intégrité des calculs financiers lors du transfert vers le Dashboard.
 * 
 * @param nom Nom du pôle (ex: Restauration).
 * @param depensesTotales Somme globale des charges constatées.
 * @param coutUnitaire Ratio coût / usager.
 * @param nombreEnfants Nombre de bénéficiaires du service.
 * @param unitesAnnuelles Volume total (repas servis, journées d'accueil, etc.).
 * @param chargesDetaillees Ventilation des coûts (Personnel, Nourriture, Fluides...).
 * @param distributionTranches Répartition des familles par tranche de Quotient Familial (QF).
 */
public record DepensePole(
    String nom,
    double depensesTotales,
    double coutUnitaire,
    Integer nombreEnfants,
    Integer unitesAnnuelles,
    Map<String, Double> chargesDetaillees,
    Map<String, Integer> distributionTranches
) {}
