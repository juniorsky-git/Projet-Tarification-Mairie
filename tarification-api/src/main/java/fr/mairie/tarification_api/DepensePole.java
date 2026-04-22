package fr.mairie.tarification_api;

import java.util.Map;

public record DepensePole(
    String nom,
    double depensesTotales,
    double coutUnitaire,
    Integer nombreEnfants,
    Integer unitesAnnuelles,
    Map<String, Double> chargesDetaillees,
    Map<String, Integer> distributionTranches
) {}
