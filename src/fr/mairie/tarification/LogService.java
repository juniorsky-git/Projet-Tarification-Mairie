package fr.mairie.tarification;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Service de journalisation compatible Java 8.
 * Permet de tracer les erreurs dans un fichier logs/erreur.log.
 */
public class LogService {
    private static final String LOG_FILE = "logs/erreur.log";

    static {
        java.io.File directory = new java.io.File("logs");
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public static void log(String message) {
        log("INFO", message, null);
    }

    public static void error(String message, Exception e) {
        log("ERROR", message, e);
    }

    private static synchronized void log(String level, String message, Exception e) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            
            pw.printf("[%s] [%s] %s%n", timestamp, level, message);
            if (e != null) {
                e.printStackTrace(pw);
            }
        } catch (Exception ex) {
            System.err.println("Impossible d'ecrire dans le fichier de log : " + ex.getMessage());
        }
    }
}
