public class Tarif {
    private String tranche;
    private double qfMin;
    private double qfMax;
    private double repas;
    private double journeeAccueilLoisirs;
    private double demiJourneeAvecRepas;
    private double matinEtSoir;
    private double matinOuSoir;
    private int usagers;
    private double recettes;

    public Tarif(String tranche, double qfMin, double qfMax, double repas, double journeeAccueilLoisirs, double demiJourneeAvecRepas, double matinEtSoir, double matinOuSoir, int usagers, double recettes) {
        this.tranche = tranche;
        this.qfMin = qfMin;
        this.qfMax = qfMax;
        this.repas = repas;
        this.journeeAccueilLoisirs = journeeAccueilLoisirs;
        this.demiJourneeAvecRepas = demiJourneeAvecRepas;
        this.matinEtSoir = matinEtSoir;
        this.matinOuSoir = matinOuSoir;
        this.usagers = usagers;
        this.recettes = recettes;
    }

    public boolean contientQf(double qf) {
        return qf >= qfMin && qf <= qfMax;
    }

    public String getTranche() { return tranche; }
    public double getQfMin() { return qfMin; }
    public double getQfMax() { return qfMax; }
    public double getRepas() { return repas; }
    public double getJourneeAccueilLoisirs() { return journeeAccueilLoisirs; }
    public double getDemiJourneeAvecRepas() { return demiJourneeAvecRepas; }
    public double getMatinEtSoir() { return matinEtSoir; }
    public double getMatinOuSoir() { return matinOuSoir; }
    public int getUsagers() { return usagers; }
    public double getRecettes() { return recettes; }
}
