package fr.mairie.tarification_api;

import org.springframework.stereotype.Service;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class LogService {

    private StringBuilder logElec = new StringBuilder("");
    private StringBuilder logGaz = new StringBuilder("");

    // --- Méthodes pour l'Audit Fluides (Instance) ---

    public void reinitialiser() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        logElec = new StringBuilder("\n=== SESSION D'AUDIT DU " + timestamp + " ===\n");
        logGaz = new StringBuilder("\n=== SESSION D'AUDIT DU " + timestamp + " ===\n");
    }

    public void ajouterLogElec(String site, int col, double montant, String periode) {
        logElec.append(String.format("[%s] Col %d : +%.2f€ (%s)\n", site, col, montant, periode));
    }

    public void ajouterLogGaz(String site, int col, double montant, String periode) {
        logGaz.append(String.format("[%s] Col %d : +%.2f€ (%s)\n", site, col, montant, periode));
    }

    public void sauvegarderFichiers() {
        try {
            Files.createDirectories(Paths.get("logs_audit"));
            Files.write(Paths.get("logs_audit/audit_electricite.log"), 
                        logElec.toString().getBytes(StandardCharsets.UTF_8), 
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            Files.write(Paths.get("logs_audit/audit_gaz.log"), 
                        logGaz.toString().getBytes(StandardCharsets.UTF_8), 
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            System.out.println("✅ Audit complété. Historique conservé dans 'logs_audit/'.");
        } catch (Exception e) {
            System.err.println("Erreur LogService : " + e.getMessage());
        }
    }

    // --- Méthodes Statiques (Requis pour le reste du projet) ---

    public static void log(String message) {
        System.out.println("[LOG] " + message);
    }

    public static void error(String message, Exception e) {
        System.err.println("[ERROR] " + message + " : " + (e != null ? e.getMessage() : "Inconnue"));
    }
}
