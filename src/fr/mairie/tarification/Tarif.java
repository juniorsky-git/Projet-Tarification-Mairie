package fr.mairie.tarification;

/**
 * Représente une tranche tarifaire avec ses plafonds de Quotient Familial
 * et les prix associés pour les différentes activités.
 */
public class Tarif {
    private String tranche;
    private double qfMin;
    private double qfMax;
    private double repas;
    private double garde;

    /**
     * Constructeur complet pour un tarif.
     * @param tranche Nom de la tranche (A, B, C...)
     * @param qfMin Quotient Familial minimum
     * @param qfMax Quotient Familial maximum
     * @param repas Prix unitaire du repas
     * @param garde Prix unitaire de la garde (optionnel)
     */
    public Tarif(String tranche, double qfMin, double qfMax, double repas, double garde) {
        this.tranche = tranche;
        this.qfMin = qfMin;
        this.qfMax = qfMax;
        this.repas = repas;
        this.garde = garde;
    }

    // --- Getters et Setters aérés ---

    /**
     * @return Le nom de la tranche tarifaire.
     */
    public String getTranche() {
        return tranche;
    }

    /**
     * @param tranche Le nom de la tranche à définir.
     */
    public void setTranche(String tranche) {
        this.tranche = tranche;
    }

    /**
     * @return Le Quotient Familial minimum de la tranche.
     */
    public double getQfMin() {
        return qfMin;
    }

    /**
     * @return Le Quotient Familial maximum de la tranche.
     */
    public double getQfMax() {
        return qfMax;
    }

    /**
     * @return Le prix du repas pour cette tranche.
     */
    public double getRepas() {
        return repas;
    }

    /**
     * @return Le prix de la garde pour cette tranche.
     */
    public double getGarde() {
        return garde;
    }
}
