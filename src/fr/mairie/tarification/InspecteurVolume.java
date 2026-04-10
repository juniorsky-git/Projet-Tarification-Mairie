package fr.mairie.tarification;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

public class InspecteurVolume {
    public static void main(String[] args) {
        String chemin = "Donnees/Autres/Detail_des_ecritures_engagements_reservations_factures_grdResultat.xls";
        System.out.println("--- Analyse de " + chemin + " ---");
        try (FileInputStream fis = new FileInputStream(chemin);
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(0);
            if (header != null) {
                for (Cell cell : header) {
                    System.out.println("[" + cell.getColumnIndex() + "] " + cell.toString());
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
        }
    }
}
