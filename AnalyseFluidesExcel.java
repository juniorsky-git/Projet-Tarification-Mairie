import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.*;
import java.util.*;

public class AnalyseFluidesExcel {

    public static void main(String[] args) {
        String csvFile = "CALC DEP(4).xlsx - CONSO GAZ.csv";
        String outputFile = "Synthese_Gaz_Java.xlsx";

        try (Workbook workbook = new XSSFWorkbook();
             BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            Sheet sheet = workbook.createSheet("Gaz Détail");
            
            // --- Préparation des Styles ---
            // Style En-tête (Orange)
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            applyBorders(headerStyle);

            // Style Alerte "et" (Rouge)
            CellStyle redStyle = workbook.createCellStyle();
            redStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            redStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font redFont = workbook.createFont();
            redFont.setColor(IndexedColors.WHITE.getIndex());
            redStyle.setFont(redFont);
            applyBorders(redStyle);

            // Style Standard (Bordures)
            CellStyle normalStyle = workbook.createCellStyle();
            applyBorders(normalStyle);

            // --- Lecture et Traitement ---
            List<String[]> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")); // Split CSV correct
            }

            // Création de l'en-tête dans Excel
            String[] headers = {"ADRESSE", "DATE FACTURE", "N° FACTURE", "FOURNISSEUR", "CONSO M3", "MONTANT TTC"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int excelRowNum = 1;
            // On simule la logique Python : parcours des blocs de colonnes
            // col 4, 8, 12... (index base 0)
            for (int colStart = 4; colStart < lines.get(0).length - 3; colStart += 4) {
                String dateF = lines.get(1)[colStart];
                String numF = lines.get(2)[colStart];
                String fourn = lines.get(3)[colStart];

                if (dateF.isEmpty() && numF.isEmpty()) continue;

                for (int i = 9; i < lines.size(); i++) {
                    String[] currentRow = lines.get(i);
                    if (currentRow.length <= colStart + 3) continue;

                    String addr = currentRow[2].replace("\"", "");
                    String consoStr = currentRow[colStart + 2];
                    String montantStr = currentRow[colStart + 3];

                    if (!consoStr.isEmpty() || !montantStr.isEmpty()) {
                        Row row = sheet.createRow(excelRowNum++);
                        
                        // Cellule Adresse + Règle du "ET"
                        Cell addrCell = row.createCell(0);
                        addrCell.setCellValue(addr);
                        if (addr.toLowerCase().contains(" et ")) {
                            addrCell.setCellStyle(redStyle);
                        } else {
                            addrCell.setCellStyle(normalStyle);
                        }

                        row.createCell(1).setCellValue(dateF);
                        row.createCell(2).setCellValue(numF);
                        row.createCell(3).setCellValue(fourn);
                        
                        // Colonnes numériques
                        try {
                            row.createCell(4).setCellValue(Double.parseDouble(consoStr.replace(",", ".")));
                            row.createCell(5).setCellValue(Double.parseDouble(montantStr.replace(",", ".")));
                        } catch (Exception e) { /* Gérer cases vides */ }
                        
                        // Appliquer bordures aux autres cellules
                        for(int j=1; j<6; j++) {
                            if(row.getCell(j) == null) row.createCell(j);
                            row.getCell(j).setCellStyle(normalStyle);
                        }
                    }
                }
            }

            // Ajuster colonnes
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            // Sauvegarde
            try (FileOutputStream fileOut = new FileOutputStream(outputFile)) {
                workbook.write(fileOut);
            }
            System.out.println("Fichier Excel généré : " + outputFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void applyBorders(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
    }
}