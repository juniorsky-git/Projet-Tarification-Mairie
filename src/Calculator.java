public class Calculator {

    // Constante déduite de l'analyse métier sur les fichiers d'export de la mairie
    private static final int NB_REPAS_AN_PAR_ENFANT = 140;

    /**
     * Calcule l'écart financier (Recettes de la mairie - Dépenses réelles)
     * Si l'écart est négatif (< 0), cela représente l'effort financier (la subvention) de la collectivité.
     */
    public static double calculerEcart(Tarif tarif) {
        return tarif.getRecettes() - tarif.getDepenses();
    }

    /**
     * Calcule le taux de couverture en pourcentage (%)
     * C'est le ratio de la dépense qui est payé par l'usager
     */
    public static double calculerTauxCouverture(Tarif tarif) {
        // Sécurité pour éviter la division par zéro mathématique
        if (tarif.getDepenses() == 0) return 0.0;
        
        return (tarif.getRecettes() / tarif.getDepenses()) * 100;
    }

    /**
     * Moteur de simulation : Calcule ce que la mairie gagnerait/perdrait si elle
     * appliquait un "nouveauPrixRepas" sur la grille tarifaire d'une tranche.
     */
    public static double simulerNouvelleRecette(Tarif tarif, double nouveauPrixRepas) {
        return tarif.getUsagers() * nouveauPrixRepas * NB_REPAS_AN_PAR_ENFANT;
    }
}
