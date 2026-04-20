package fr.mairie.tarification_api;

public class TarifResponse {
    public String tranche;
    public double qfMin;
    public double qfMax;

    // Restauration
    public double repas;

    // Accueil de loisirs
    public double accueilJournee;
    public double accueilDemiRepas;

    // Périscolaire
    public double periscolaireMatinSoir;
    public double periscolaireMatinOuSoir;

    // Études surveillées
    public double etudesForfaitMensuel;
    public double etudesDemiForfait;
    public double tarifPostEtudes;
    public double classeDecouverte;

    // Jeunesse / Espace ados
    public double adosVacJourneeRepas;
    public double adosVacJourneeSans;
    public double adosVacDemiRepas;
    public double adosVacDemiSans;
    public double adosSortieDemi;
    public double adosSortieJournee;

    // Séjours
    public double sejour5Jours;
    public double sejour6Jours;
}