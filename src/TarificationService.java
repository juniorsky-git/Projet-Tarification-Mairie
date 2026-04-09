import java.util.List;

public class TarificationService {

    public Tarif trouverTarif(double qf, List<Tarif> tarifs) {
        for (Tarif tarif : tarifs) {
            if (tarif.contientQf(qf)) {
                return tarif;
            }
        }
        return null;
    }

    public double obtenirPrix(Tarif tarif, String activite) {
        if (tarif == null) {
            throw new IllegalArgumentException("Aucun tarif trouvé.");
        }

        switch (activite.toLowerCase()) {
            case "repas":
                return tarif.getRepas();
            case "journee":
                return tarif.getJourneeAccueilLoisirs();
            case "demi-journee":
                return tarif.getDemiJourneeAvecRepas();
            case "matin-et-soir":
                return tarif.getMatinEtSoir();
            case "matin-ou-soir":
                return tarif.getMatinOuSoir();
            default:
                throw new IllegalArgumentException("Activité inconnue : " + activite);
        }
    }
}