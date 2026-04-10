package fr.mairie.tarification;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Tarif {
    private String tranche;
    private double qfMin;
    private double qfMax;

    // Données statistiques (depuis le CSV Classeur1)
    private int usagers;
    private double recettes;

    // Stockage de tous les tarifs par activité (HashMap)
    private Map<String, Double> tarifs;

    // --- Constructeur principal (HashMap) ---
    public Tarif(String tranche, double qfMin, double qfMax, Map<String, Double> tarifs) {
        this.tranche = tranche;
        this.qfMin = qfMin;
        this.qfMax = qfMax;
        this.tarifs = new HashMap<>(tarifs);
        this.usagers = 0;
        this.recettes = 0.0;
    }

    // --- Constructeur avec stats CSV ---
    public Tarif(String tranche, double qfMin, double qfMax, Map<String, Double> tarifs, int usagers, double recettes) {
        this(tranche, qfMin, qfMax, tarifs);
        this.usagers = usagers;
        this.recettes = recettes;
    }

    // --- Méthodes ---
    public boolean contientQf(double qf) {
        return qf >= qfMin && qf <= qfMax;
    }

    /**
     * Retourne le prix pour une activité donnée.
     * @param activite la clé de l'activité (ex: "repas", "etudes-forfait-mensuel", "ados-journee-repas")
     * @return le prix, ou 0.0 si l'activité n'est pas définie pour cette tranche
     */
    public double getPrix(String activite) {
        return tarifs.getOrDefault(activite.toLowerCase(), 0.0);
    }

    public boolean aActivite(String activite) {
        return tarifs.containsKey(activite.toLowerCase());
    }

    public Set<String> getActivitesDisponibles() {
        return tarifs.keySet();
    }

    // --- Getters ---
    public String getTranche()    { return tranche; }
    public double getQfMin()      { return qfMin; }
    public double getQfMax()      { return qfMax; }
    public int getUsagers()       { return usagers; }
    public double getRecettes()   { return recettes; }

    // Retrocompatibilité
    public double getRepas()                  { return getPrix("repas"); }
    public double getJourneeAccueilLoisirs()  { return getPrix("accueil-journee"); }
    public double getDemiJourneeAvecRepas()   { return getPrix("accueil-demi-repas"); }
    public double getMatinEtSoir()            { return getPrix("periscolaire-matin-soir"); }
    public double getMatinOuSoir()            { return getPrix("periscolaire-matin-ou-soir"); }
}
