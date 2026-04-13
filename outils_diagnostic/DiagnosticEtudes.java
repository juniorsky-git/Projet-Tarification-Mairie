package fr.mairie.tarification.outils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;

/**
 * Diagnostic technique pour le pole Etudes Surveillees.
 * Va chercher les donnees dans le CSV de secours car le fichier Excel principal
 * presente des erreurs de formules (#REF!).
 */
public class DiagnosticEtudes {

    private static final String CSV_FILE = "Donnees/Tableau-grille/Classeur1.csv";

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   DIAGNOSTIC TECHNIQUE : ETUDES SURVEILLEES   ");
        System.out.println("=================================================\n");

        boolean blockFound = false;
        try (BufferedReader br = new BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(CSV_FILE), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("ETUDES SURVEILLEES")) {
                    blockFound = true;
                    System.out.println("[OK] Bloc 'ETUDES SURVEILLEES' identifie dans le CSV.");
                    continue;
                }

                if (blockFound) {
                    if (line.contains("Total g") || line.contains("personnel")) {
                        String[] parts = line.split(";");
                        if (parts.length > 2) {
                            String label = parts[0].isEmpty() ? "Charges de Personnel" : parts[0];
                            String montant = parts[2].replace("", " ").trim();
                            
                            if (!montant.isEmpty() && !montant.equals("0") && !montant.equals("0,00")) {
                                System.out.printf("   %-25s : %s EUR%n", label, montant);
                            }
                        }
                    }
                    
                    // Fin du bloc
                    if (line.contains("ESPACE ADOS")) break;
                }
            }
            
            if (!blockFound) {
                System.err.println("[ERREUR] Impossible de trouver les donnees Etudes dans le CSV.");
            }

            System.out.println("\n-------------------------------------------------");
            System.out.println("LOGIQUE : Extraction forcée depuis CSV car");
            System.out.println("l'onglet Simulation présente des erreurs #REF!.");
            System.out.println("-------------------------------------------------");

        } catch (Exception e) {
            System.err.println("Erreur lecture diagnostic : " + e.getMessage());
        }
    }
}
