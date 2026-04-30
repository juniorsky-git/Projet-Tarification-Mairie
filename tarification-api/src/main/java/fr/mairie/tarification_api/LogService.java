package fr.mairie.tarification_api;

import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
     */
    public synchronized void sauvegarderFichiers() {
        try {
            Files.createDirectories(Paths.get("logs_audit"));

            // On vérifie si on a quelque chose à écrire pour éviter les accès disque inutiles
            if (logElec.length() > 50) {
                Files.write(Paths.get("logs_audit/audit_electricite.log"), 
                            logElec.toString().getBytes(StandardCharsets.UTF_8), 
                            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }

            if (logGaz.length() > 50) {
                Files.write(Paths.get("logs_audit/audit_gaz.log"), 
                            logGaz.toString().getBytes(StandardCharsets.UTF_8), 
                            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
            
            // On vide les buffers après écriture pour ne pas réécrire 10 fois la même chose
            logElec.setLength(0);
            logGaz.setLength(0);

        } catch (Exception e) {
            System.err.println("Erreur LogService lors de l'écriture : " + e.getMessage());
        }
    }

    /**
     * Lit le contenu des logs techniques de manière ultra-rapide (lecture par la fin).
     */
    public String lireDerniersLogs() {
        StringBuilder sb = new StringBuilder();
        sb.append(lireFinFichier("logs_audit/audit_electricite.log", "ÉLECTRICITÉ"));
        sb.append("\n");
        sb.append(lireFinFichier("logs_audit/audit_gaz.log", "GAZ"));
        return sb.toString();
    }

    private String lireFinFichier(String chemin, String titre) {
        Path path = Paths.get(chemin);
        if (!Files.exists(path)) return "--- AUCUN AUDIT " + titre + " ---\n";

        try (java.util.stream.Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            StringBuilder temp = new StringBuilder("--- DERNIERS AUDITS " + titre + " ---\n");
            // On ne garde que les 40 dernières lignes pour la performance
            Object[] lastLines = lines.skip(Math.max(0, Files.lines(path).count() - 40)).toArray();
            for (Object line : lastLines) {
                temp.append(line).append("\n");
            }
            return temp.toString();
        } catch (Exception e) {
            return "--- ERREUR DE LECTURE " + titre + " ---\n";
        }
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
