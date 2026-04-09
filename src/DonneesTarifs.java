import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DonneesTarifs {

    public static List<Tarif> chargerTarifs() {
        List<Tarif> tarifs = new ArrayList<>();
        String cheminFichier = "Donnees/tarifs.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(cheminFichier))) {
            String ligne;
            boolean premiereLigne = true;

            while ((ligne = br.readLine()) != null) {
                // Ignorer l'en-tête
                if (premiereLigne) {
                    premiereLigne = false;
                    continue;
                }

                // Ignorer les lignes vides
                if (ligne.trim().isEmpty()) {
                    continue;
                }

                // Découper la ligne avec le point-virgule
                String[] valeurs = ligne.split(";");

                if (valeurs.length >= 8) {
                    String tranche = valeurs[0];
                    double qfMin = Double.parseDouble(valeurs[1]);
                    double qfMax = Double.parseDouble(valeurs[2]);
                    double repas = Double.parseDouble(valeurs[3]);
                    double journee = Double.parseDouble(valeurs[4]);
                    double demiJournee = Double.parseDouble(valeurs[5]);
                    double matinSoir = Double.parseDouble(valeurs[6]);
                    double matinOuSoir = Double.parseDouble(valeurs[7]);

                    tarifs.add(new Tarif(tranche, qfMin, qfMax, repas, journee, demiJournee, matinSoir, matinOuSoir));
                }
            }
        } catch (IOException e) {
            System.err.println("Avertissement : Erreur de lecture du fichier " + cheminFichier + " (" + e.getMessage() + ")");
            System.err.println("Assurez-vous que le programme est lancé depuis la racine du projet.");
        } catch (NumberFormatException e) {
            System.err.println("Avertissement : Erreur de format dans le fichier CSV (" + e.getMessage() + ")");
        }

        return tarifs;
    }
}