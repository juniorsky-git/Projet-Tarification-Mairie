package fr.mairie.tarification;

import java.util.Map;

/**
 * Représente une tranche tarifaire avec sa grille de prix multi-services.
 * Chaque tranche possède un nom (A, B, C...) et une Map associant 
 * un service (REPAS, ADOS, LOISIRS...) à son prix unitaire.
 * 
 * @author Séri-khane YOLOU
 * @version 1.2
 */
public class Tarif {
    private String tranche;
    private double qfMin;
    private double qfMax;
    private Map<String, Double> prixParService;

    /**
     * Constructeur multi-services.
     * @param tranche Nom de la tranche
     * @param qfMin Quotient Familial minimum
     * @param qfMax Quotient Familial maximum
     * @param prixParService Map contenant les prix de chaque service
     */
    public Tarif(String tranche, double qfMin, double qfMax, Map<String, Double> prixParService) {
        this.tranche = tranche;
        this.qfMin = qfMin;
        this.qfMax = qfMax;
        this.prixParService = prixParService;
    }

    /**
     * @return Le nom de la tranche tarifaire.
     */
    public String getTranche() {
        return tranche;
    }

    /**
     * @return Le Quotient Familial minimum.
     */
    public double getQfMin() {
        return qfMin;
    }

    /**
     * @return Le Quotient Familial maximum.
     */
    public double getQfMax() {
        return qfMax;
    }

    /**
     * Recupere le prix pour un service specifique.
     * 
     * @param service Nom du service (ex: REPAS, ADOS_VAC_JOURNEE...).
     * @return Le prix unitaire, ou 0 si le service n'est pas defini pour cette tranche.
     */
    public double getPrix(String service) {
        if (prixParService != null) {
            if (prixParService.containsKey(service)) {
                return prixParService.get(service);
            } else {
                return 0.0;
            }
        } else {
            return 0.0;
        }
    }

    /**
     * Raccourci pour le prix du repas (compatibilité).
     * @return Le prix du repas scolaire.
     */
    public double getRepas() {
        return getPrix("REPAS");
    }

    /**
     * Raccourci pour le prix de la garde (compatibilité).
     * @return Le prix de l'accueil de loisirs journée.
     */
    public double getGarde() {
        return getPrix("ACCUEIL_JOURNEE");
    }
}
