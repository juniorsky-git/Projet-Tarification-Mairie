package fr.mairie.tarification;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Outil de diagnostic initial pour la lecture des fichiers Excel.
 * Note : Cette classe a été remplacée par Calculateur.java pour l'application finale.
 */
public class ExcelReader {

    /**
     * Lit un fichier Excel et tente d'en extraire les tarifs.
     * @param chemin Le chemin vers le fichier .xlsx
     * @return Une liste de tarifs extraits.
     */
    public List<Tarif> lireTarifs(String chemin) {
        List<Tarif> tarifs = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(chemin);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                // Lecture simplifiée pour le diagnostic
                String codeTranche = row.getCell(0).toString();
                double prixRepas = row.getCell(2).getNumericCellValue();

                // On crée un tarif avec des valeurs par défaut pour les tests
                tarifs.add(new Tarif(codeTranche, 0, 1000000, prixRepas, 0));
            }
        } catch (Exception e) {
            System.err.println("Erreur de lecture : " + e.getMessage());
        }
        return tarifs;
    }
}
