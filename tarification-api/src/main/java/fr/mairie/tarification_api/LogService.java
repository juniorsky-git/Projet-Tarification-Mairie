package fr.mairie.tarification_api;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Service de journalisation pour l'API de tarification.
 *
 * Écrit les logs INFO et ERROR dans un fichier dédié (logs/api-erreur.log)
 * et affiche également les messages sur la sortie d'erreur standard.
 *
 * @author Séri-khane YOLOU
 * @version 1.0
 */
public class LogService {

    /** Chemin vers le fichier de log de l'API. */
    private static final String LOG_FILE = "logs/api-erreur.log";

    static {
        java.io.File directory = new java.io.File("logs");
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * Enregistre un message d'information.
     *
     * @param message Message à journaliser.
     */
    public static void log(String message) {
        log("INFO", message, null);
    }

    /**
     * Enregistre une erreur avec sa trace d'exception.
     *
     * @param message Description de l'erreur.
     * @param e       L'exception générée.
     */
    public static void error(String message, Exception e) {
        log("ERROR", message, e);
    }

    /**
     * Méthode interne d'écriture synchronisée dans le fichier de log.
     *
     * @param level   Niveau de log (INFO ou ERROR).
     * @param message Message à écrire.
     * @param e       Exception optionnelle (peut être null).
     */
    private static synchronized void log(String level, String message, Exception e) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String ligne = String.format("[%s] [%s] %s", timestamp, level, message);

        // Affichage console
        System.err.println(ligne);
        if (e != null) e.printStackTrace();

        // Écriture dans le fichier
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(ligne);
            if (e != null) e.printStackTrace(pw);
        } catch (Exception ex) {
            System.err.println("Impossible d'écrire dans le fichier de log : " + ex.getMessage());
        }
    }
}
