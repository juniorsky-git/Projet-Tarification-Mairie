package fr.mairie.tarification_api;

import java.util.Map;

public class DashboardResponse {
    public String pole;
    public double depensesTotales;
    public double coutUnitaire;
    public Integer nombreEnfants;
    public Integer unitesAnnuelles;
    public Map<String, Double> detailsCharges;
}