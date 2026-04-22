package fr.mairie.tarification_api;

import java.util.List;
import java.util.Map;

public class DashboardResponse {
    public String pole;
    public double depensesTotales;
    public double coutUnitaire;
    public Integer nombreEnfants;
    public Integer unitesAnnuelles;
    public Map<String, Double> detailsCharges;
    public List<AnalytiqueFluide> detailsFluides;
    
    // Nouveaux indicateurs financiers
    public double recettesTotales;
    public double tauxCouverture;
    public double ecart;
    public Map<String, Integer> distributionTranches;
}