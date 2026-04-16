import fr.mairie.tarification.*;
import java.util.List;

public class TestCasReels {
    public static void main(String[] args) {
        String fichier = "Donnees/Grille-tarifaire-2024-(1).xlsx";
        System.out.println("=== CHARGEMENT DE LA GRILLE 2024 ===");
        List<Tarif> grille = DonneesTarifs.chargerGrilleStandard(fichier);
        
        if (grille.isEmpty()) {
            System.out.println("Erreur de chargement.");
            return;
        }

        TarificationService service = new TarificationService();
        double[] qfsATester = { 18000.0, 10000.0, 4500.0, 0.0 };
        String[] cibles = { "Tranche A (>= 15 389)", "Tranche D (8 527 -> 10 813)", "Tranche F (3 954 -> 6 240)", "Tranche G (< 3 954)" };

        for (int i = 0; i < qfsATester.length; i++) {
            double qf = qfsATester[i];
            System.out.println("\n--- TEST CAS N*" + (i+1) + " : QF = " + qf + " | Cible attendue : " + cibles[i] + " ---");
            
            try {
                Tarif t = service.trouverTarif(qf, grille);
                if (t != null) {
                    System.out.println("-> TRANCHE OBTENUE : " + t.getTranche());
                    System.out.println("Restauration : " + t.getPrix(DonneesTarifs.REPAS) + " euros");
                    System.out.println("Loisirs Journee : " + t.getPrix(DonneesTarifs.ACCUEIL_JOURNEE) + " euros");
                    System.out.println("Sejour 5 jours : " + t.getPrix(DonneesTarifs.SEJOUR_5_JOURS) + " euros");
                } else {
                    System.out.println("ERREUR : Aucune tranche trouvee.");
                }
            } catch (Exception e) {
                System.out.println("Exception: " + e.getMessage());
            }
        }
    }
}
