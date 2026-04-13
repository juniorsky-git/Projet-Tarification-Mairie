package fr.mairie.tarification.outils;

import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

public class AnalyseLogiqueFiltrage {
    private static final String FICHIER = "Donnees/Autres/CALC DEP.xlsx";

    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream(FICHIER);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet s = wb.getSheetAt(0);
            double totalAND = 0;
            double totalOR = 0;
            int countAND = 0;
            int countOR = 0;

            String[] exclusions = {"ADOS", "LOISIRS", "COMMUNAL"};

            for (int i = 1; i <= s.getLastRowNum(); i++) {
                Row row = s.getRow(i);
                if (row == null) continue;

                String ant = getCellString(row.getCell(19)); // Col T
                String ser = getCellString(row.getCell(18)); // Col S
                String lib = getCellString(row.getCell(3)).toUpperCase();

                boolean isExcl = false;
                for (String ex : exclusions) {
                    if (lib.contains(ex)) { isExcl = true; break; }
                }
                if (isExcl) continue;

                double montant = Math.abs(getCellDouble(row.getCell(7))); // Col H

                // Logique AND (VerificateurValeurs actuelle)
                if (ant.equalsIgnoreCase("RESTMICH") && ser.contains("2-RE")) {
                    totalAND += montant;
                    countAND++;
                }

                // Logique OR (Calculateur.java universelle)
                if (ant.equalsIgnoreCase("RESTMICH") || ser.contains("2-RE")) {
                    totalOR += montant;
                    countOR++;
                }
            }

            System.out.println("Logique AND (ANTENNE && SERVICE) : " + countAND + " lignes | Total : " + totalAND);
            System.out.println("Logique OR  (ANTENNE || SERVICE) : " + countOR + " lignes | Total : " + totalOR);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getCellString(Cell c) {
        if (c == null) return "";
        return c.toString().trim();
    }

    private static double getCellDouble(Cell c) {
        if (c == null) return 0;
        try {
            if (c.getCellType() == CellType.NUMERIC) return c.getNumericCellValue();
            String s = c.toString().replace(",", ".");
            return Double.parseDouble(s);
        } catch (Exception e) { return 0; }
    }
}
