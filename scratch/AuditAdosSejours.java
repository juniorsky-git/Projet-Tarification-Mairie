
import fr.mairie.tarification.Calculateur;
import java.util.Map;

public class AuditAdosSejours {
    public static void main(String[] args) {
        Calculateur calc = new Calculateur();
        Calculateur.SyntheseGlobale sg = calc.getSynthese();
        
        System.out.println("--- TARIFS ESPACE ADOS & SEJOURS (Source: syntheses charges) ---");
        System.out.println("Tranche | Espace Ados | Sejours");
        System.out.println("---------------------------------");
        
        for (String tranche : sg.tarifs.keySet()) {
            Map<String, Double> poleTarifs = sg.tarifs.get(tranche);
            double ados = poleTarifs.getOrDefault("Espace Ados", 0.0);
            double sejours = poleTarifs.getOrDefault("Sejours", 0.0);
            System.out.printf("%-10s | %11.2f | %11.2f%n", tranche, ados, sejours);
        }
    }
}
