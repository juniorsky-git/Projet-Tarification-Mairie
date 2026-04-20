package fr.mairie.tarification_api;

import java.util.Map;

/**
 * Représente une tranche tarifaire avec sa grille de prix multi-services.
 * Chaque tranche possède un nom (A, B, C...) et une Map associant
 * un service (REPAS, ADOS, LOISIRS...) à son prix unitaire.
 *
 * @author Séri-khane YOLOU
 * @version 1.3
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

    public String getTranche() {
        return tranche;
    }

    public double getQfMin() {
        return qfMin;
    }

    public double getQfMax() {
        return qfMax;
    }

    /**
     * Récupère le prix pour un service spécifique.
     * @param service Nom technique du service
     * @return Le prix unitaire, ou 0.0 si non défini
     */
    public double getPrix(String service) {
        if (prixParService != null && prixParService.containsKey(service)) {
            return prixParService.get(service);
        }
        return 0.0;
    }

    // =========================
    // RESTAURATION
    // =========================
    public double getRepas() {
        return getPrix("REPAS");
    }

    // =========================
    // ACCUEIL DE LOISIRS
    // =========================
    public double getAccueilJournee() {
        return getPrix("ACCUEIL_JOURNEE");
    }

    public double getAccueilDemiRepas() {
        return getPrix("ACCUEIL_DEMI_REPAS");
    }

    /**
     * Compatibilité avec ton ancien affichage.
     */
    public double getGarde() {
        return getAccueilJournee();
    }

    // =========================
    // PÉRISCOLAIRE
    // =========================
    public double getPeriscolaireMatinSoir() {
        return getPrix("PERISCOLAIRE_MATIN_SOIR");
    }

    public double getPeriscolaireMatinOuSoir() {
        return getPrix("PERISCOLAIRE_MATIN_OU_SOIR");
    }

    // =========================
    // ÉTUDES SURVEILLÉES
    // =========================
    public double getEtudesForfaitMensuel() {
        return getPrix("ETUDES_FORFAIT_MENSUEL");
    }

    public double getEtudesDemiForfait() {
        return getPrix("ETUDES_DEMI_FORFAIT");
    }

    public double getTarifPostEtudes() {
        return getPrix("TARIF_POST_ETUDES");
    }

    public double getClasseDecouverte() {
        return getPrix("CLASSE_DECOUVERTE");
    }

    // =========================
    // JEUNESSE / ESPACE ADOS
    // =========================
    public double getAdosVacJourneeRepas() {
        return getPrix("ADOS_VAC_JOURNEE_REPAS");
    }

    public double getAdosVacJourneeSans() {
        return getPrix("ADOS_VAC_JOURNEE_SANS");
    }

    public double getAdosVacDemiRepas() {
        return getPrix("ADOS_VAC_DEMI_REPAS");
    }

    public double getAdosVacDemiSans() {
        return getPrix("ADOS_VAC_DEMI_SANS");
    }

    public double getAdosSortieDemi() {
        return getPrix("ADOS_SORTIE_DEMI");
    }

    public double getAdosSortieJournee() {
        return getPrix("ADOS_SORTIE_JOURNEE");
    }

    // =========================
    // SÉJOURS
    // =========================
    public double getSejour5Jours() {
        return getPrix("SEJOUR_5_JOURS");
    }

    public double getSejour6Jours() {
        return getPrix("SEJOUR_6_JOURS");
    }
}