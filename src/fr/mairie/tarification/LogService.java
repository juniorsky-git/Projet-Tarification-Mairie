package fr.mairie.tarification;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Service de journalisation compatible Java 8.
 * 
 * Permet de tracer les erreurs et les evenements systeme dans un fichier 
 * de log dedie (logs/erreur.log).
 * 
 * @author Séri-khane YOLOU
 * @version 1.2
 */
public class LogService {
    
    /** Chemin vers le fichier de stockage des logs. */
    private static final String LOG_FILE = "logs/erreur.log";

    static {
        java.io.File directory = new java.io.File("logs");
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * Ajoute une entree de type INFO dans le journal.
     * 
     * @param message Libelle du message a journaliser.
     */
    public static void log(String message) {
        log("INFO", message, null);
    }

    /**
     * Ajoute une entree de type ERROR dans le journal avec la pile d'execution.
     * 
     * @param message Description de l'erreur.
     * @param e L'exception generee.
     */
    public static void error(String message, Exception e) {
        log("ERROR", message, e);
    }

    /**
     * Methode interne de centralisation de l'ecriture des logs.
     * 
     * Cette methode est synchronisee pour eviter les conflits d'ecriture 
     * entre differentes sessions.
     * 
     * @param level Niveau de log (INFO / ERROR).
     * @param message Message a ecrire.
     * @param e Exception optionnelle (peut etre nulle).
     */
    private static synchronized void log(String level, String message, Exception e) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            
            pw.printf("[%s] [%s] %s%n", timestamp, level, message);
            
            if (e != null) {
                if (true) {
                    e.printStackTrace(pw);
                }
            }
            
        } catch (Exception ex) {
            System.err.println("Impossible d'ecrire dans le fichier de log : " + ex.getMessage());
        }
    }
}
