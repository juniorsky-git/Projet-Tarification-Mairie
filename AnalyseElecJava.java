import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.util.*;

public class AnalyseElecJava {

    public static void main(String[] args) {
        String csvFile = "CALC DEP(4).xlsx - CONSO ELEC.csv";
        String outputFile = "Synthese_ELEC_Java_Final.xlsx";

        try (Workbook workbook = new XSSFWorkbook();
             BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            Sheet sheet = workbook.createSheet("ELEC Détail");

            // --- 1. Création des Styles ---
            // En-tête Bleu Foncé
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex()); // Bleu Pro
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            applyBorders(headerStyle);

            // Alerte Rouge pour le "et"
            CellStyle redStyle = workbook.createCellStyle();
            redStyle.setFillForegroundColor(IndexedColors.RED1.getIndex());
            redStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font redFont = workbook.createFont();
            redFont.setColor(IndexedColors.WHITE.getIndex());
            redStyle.setFont(redFont);
            applyBorders(redStyle);

            CellStyle normalStyle = workbook.createCellStyle();
            applyBorders(normalStyle);

            // --- 2. Lecture du CSV ---
            List<String[]> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                // Split prenant en compte les virgules dans les guillemets
                lines.add(line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"));
            }

            // --- 3. Création des En-têtes dans Excel ---
            String[] headers = {"NOM - SITE", "REFERENCE PDL", "ADRESSE", "DATE FACTURE", "N° FACTURE", 
                                "PERIODE CONSO", "PERIODE ABO", "KWH", "MONTANT TTC"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int excelRowIdx = 1;

            // --- 4. Logique d'extraction (Scan horizontal) ---
            // On commence à la colonne 7 (index des premières données de facture)
            for (int colBase = 7; colBase < lines.get(0).length - 3; colBase += 4) {
                
                // Récupération Date et Numéro (Lignes 1 et 2 dans le CSV original)
                String dateF = lines.get(1)[colBase].trim();
                String numF = lines.get(2)[colBase].trim();

                if (dateF.isEmpty() && numF.isEmpty()) continue;

                // Parcours des sites (Lignes 9+)
                for (int i = 9; i < lines.size(); i++) {
                    String[] data = lines.get(i);
                    if (data.length <= colBase + 3) continue;

                    String nomSite = data[0].replace("\"", "");
                    String pdl = data[1];
                    String adresse = data[5].replace("\"", "");
                    String pConso = data[colBase];
                    String pAbo = data[colBase + 1];
                    String kwhStr = data[colBase + 2].replace(",", ".");
                    String ttcStr = data[colBase + 3].replace(",", ".");

                    // On n'ajoute que s'il y a des kWh
                    if (!kwhStr.isEmpty() && !kwhStr.equals("0") && !kwhStr.equals("0.0")) {
                        Row row = sheet.createRow(excelRowIdx++);
                        
                        // Remplissage des cellules
                        createStyledCell(row, 0, nomSite, normalStyle, redStyle);
                        row.createCell(1).setCellValue(pdl);
                        createStyledCell(row, 2, adresse, normalStyle, redStyle);
                        row.createCell(3).setCellValue(dateF);
                        row.createCell(4).setCellValue(numF);
                        row.createCell(5).setCellValue(pConso);
                        row.createCell(6).setCellValue(pAbo);
                        
                        try {
                            row.createCell(7).setCellValue(Double.parseDouble(kwhStr));
                            row.createCell(8).setCellValue(Double.parseDouble(ttcStr));
                        } catch (Exception e) {}

                        // Appliquer bordures partout
                        for(int j=1; j<9; j++) {
                            if(row.getCell(j) != null) row.getCell(j).setCellStyle(normalStyle);
                        }
                    }
                }
            }

            // Auto-ajustement
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            // --- 5. Enregistrement ---
            try (FileOutputStream fileOut = new FileOutputStream(outputFile)) {
                workbook.write(fileOut);
            }
            System.out.println("Java : Fichier Élec généré avec succès !");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createStyledCell(Row row, int col, String value, CellStyle normal, CellStyle red) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        if (value.toLowerCase().contains(" et ")) {
            cell.setCellStyle(red);
        } else {
            cell.setCellStyle(normal);
        }
    }

    private static void applyBorders(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
    }
}