
import java.util.List;

public class Main {
    public static void main(String[] args) {
        double qf = 7500;
        String activite = "repas";

        List<Tarif> tarifs = DonneesTarifs.chargerTarifs();
        TarificationService service = new TarificationService();

        Tarif tarif = service.trouverTarif(qf, tarifs);

        if (tarif == null) {
            System.out.println("Aucune tranche trouvée pour le QF : " + qf);
            return;
        }

        double prix = service.obtenirPrix(tarif, activite);

        System.out.println("QF : " + qf);
        System.out.println("Tranche : " + tarif.getTranche());
        System.out.println("Activité : " + activite);
        System.out.println("Tarif : " + prix + " €");
    }
}