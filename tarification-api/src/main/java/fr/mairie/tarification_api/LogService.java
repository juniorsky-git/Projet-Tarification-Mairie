package fr.mairie.tarification_api;

import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service de gestion de la traçabilité et de l'audit.
 * 
 * Ce service permet de journaliser les extractions de données en temps réel 
 * et de générer des fichiers de preuves (.log) pour justifier les calculs financiers.
 * 
 * @author Stagiaire DG 2
 */
@Service
public class LogService {

    /** Mémoire temporaire pour les logs d'électricité */
    private StringBuilder logElec = new StringBuilder("");
    
    /** Mémoire temporaire pour les logs de gaz */
    private StringBuilder logGaz = new StringBuilder("");

    /**
     * Réinitialise les buffers de log avec un nouvel horodatage.
     * Appelé à chaque nouveau lancement de diagnostic.
     */
    public void reinitialiser() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        logElec = new StringBuilder("\n=== SESSION D'AUDIT DU " + timestamp + " ===\n");
        logGaz = new StringBuilder("\n=== SESSION D'AUDIT DU " + timestamp + " ===\n");
    }

    /**
     * Journalise une facture d'électricité identifiée.
     * @param site Nom du bâtiment
     * @param col Numéro de la colonne Excel (Source)
     * @param montant Montant TTC détecté
     * @param periode Plage de dates de facturation
     */
    public void ajouterLogElec(String site, int col, double montant, String periode) {
        logElec.append(String.format("[%s] Col %d : +%.2f€ (%s)\n", site, col, montant, periode));
    }

    /**
     * Journalise une facture de gaz identifiée.
     * @param site Nom du bâtiment
     * @param col Numéro de la colonne Excel (Source)
     * @param montant Montant TTC détecté
     * @param periode Plage de dates de facturation
     */
    public void ajouterLogGaz(String site, int col, double montant, String periode) {
        logGaz.append(String.format("[%s] Col %d : +%.2f€ (%s)\n", site, col, montant, periode));
    }

    /**
     * Exporte les buffers de log vers des fichiers physiques dans le dossier 'logs_audit/'.
     * Utilise l'option APPEND pour conserver l'historique des sessions.
     */
    public void sauvegarderFichiers() {
        try {
            // Création du dossier s'il n'existe pas
            Files.createDirectories(Paths.get("logs_audit"));

            // Écriture pour l'électricité
            Files.write(Paths.get("logs_audit/audit_electricite.log"), 
                        logElec.toString().getBytes(StandardCharsets.UTF_8), 
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            // Écriture pour le gaz
            Files.write(Paths.get("logs_audit/audit_gaz.log"), 
                        logGaz.toString().getBytes(StandardCharsets.UTF_8), 
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            System.out.println("✅ Audit complété. Historique conservé dans 'logs_audit/'.");
        } catch (Exception e) {
            System.err.println("Erreur LogService lors de l'écriture : " + e.getMessage());
        }
    }

    /**
     * Lit le contenu des logs techniques pour l'affichage dans l'Historique.
     * @return Une chaîne formatée contenant les derniers audits.
     */
    public String lireDerniersLogs() {
        StringBuilder sb = new StringBuilder();
        try {
            if (Files.exists(Paths.get("logs_audit/audit_electricite.log"))) {
                sb.append("--- AUDIT ÉLECTRICITÉ ---\n");
                List<String> lines = Files.readAllLines(Paths.get("logs_audit/audit_electricite.log"));
                // On prend les 50 dernières lignes pour ne pas surcharger
                int start = Math.max(0, lines.size() - 50);
                for(int i=start; i<lines.size(); i++) sb.append(lines.get(i)).append("\n");
            }
            sb.append("\n");
            if (Files.exists(Paths.get("logs_audit/audit_gaz.log"))) {
                sb.append("--- AUDIT GAZ ---\n");
                List<String> lines = Files.readAllLines(Paths.get("logs_audit/audit_gaz.log"));
                int start = Math.max(0, lines.size() - 50);
                for(int i=start; i<lines.size(); i++) sb.append(lines.get(i)).append("\n");
            }
        } catch (Exception e) {
            return "Erreur lors de la lecture des logs techniques.";
        }
        return sb.toString();
    }

    // --- Méthodes Statiques d'utilité générale ---

    /**
     * Log informatif simple dans la console système.
     */
    public static void log(String message) {
        System.out.println("[LOG] " + message);
    }

    /**
     * Log d'erreur dans la sortie d'erreur standard.
     */
    public static void error(String message, Exception e) {
        String detailedMessage = (e != null) ? e.getMessage() : "Cause inconnue";
        System.err.println("[ERROR] " + message + " : " + detailedMessage);
    }
}
