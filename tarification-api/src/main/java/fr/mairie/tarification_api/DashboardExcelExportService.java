package fr.mairie.tarification_api;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class DashboardExcelExportService {

    public byte[] exporterDashboard(Integer anneeActive, List<DepensePole> poles, Map<String, Double> recettesReelles, Map<String, Map<String, Double>> totauxSaisie, List<DashboardResponse> dashboardResponses) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // --- Styles ---
            CellStyle styleTitre = workbook.createCellStyle();
            Font fontTitre = workbook.createFont();
            fontTitre.setBold(true);
            fontTitre.setFontHeightInPoints((short) 14);
            fontTitre.setColor(IndexedColors.WHITE.getIndex());
            styleTitre.setFont(fontTitre);
            styleTitre.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            styleTitre.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle styleEntete = workbook.createCellStyle();
            Font fontEntete = workbook.createFont();
            fontEntete.setBold(true);
            styleEntete.setFont(fontEntete);
            styleEntete.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            styleEntete.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleEntete.setBorderBottom(BorderStyle.THIN);

            CellStyle styleSousTitre = workbook.createCellStyle();
            Font fontSous = workbook.createFont();
            fontSous.setBold(true);
            fontSous.setColor(IndexedColors.DARK_BLUE.getIndex());
            styleSousTitre.setFont(fontSous);
            styleSousTitre.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
            styleSousTitre.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle styleMontant = workbook.createCellStyle();
            styleMontant.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00 €"));

            CellStyle styleTotal = workbook.createCellStyle();
            Font fontTotal = workbook.createFont();
            fontTotal.setBold(true);
            fontTotal.setFontHeightInPoints((short) 11);
            styleTotal.setFont(fontTotal);
            styleTotal.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            styleTotal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleTotal.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00 €"));
            styleTotal.setBorderTop(BorderStyle.MEDIUM);

            CellStyle stylePourcentage = workbook.createCellStyle();
            Font fontPct = workbook.createFont();
            fontPct.setBold(true);
            stylePourcentage.setFont(fontPct);
            stylePourcentage.setDataFormat(workbook.createDataFormat().getFormat("0.0%"));
            stylePourcentage.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            stylePourcentage.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // ==========================================
            // FEUILLE 1 : RAPPORT FINANCIER DÉTAILLÉ
            // ==========================================
            Sheet sheetReport = workbook.createSheet("Rapport Financier " + (anneeActive != null ? anneeActive : "2025"));
            sheetReport.setColumnWidth(0, 10000); // Libellé
            sheetReport.setColumnWidth(1, 5000);  // Montant
            sheetReport.setColumnWidth(2, 6000);  // Info Sup

            // Titre principal
            Row rowTitre = sheetReport.createRow(0);
            Cell cellTitre = rowTitre.createCell(0);
            cellTitre.setCellValue("RAPPORT FINANCIER DÉTAILLÉ — Mairie de Crosne — " + (anneeActive != null ? anneeActive : "2025"));
            cellTitre.setCellStyle(styleTitre);
            
            int rowIdx = 2;

            for (DashboardResponse r : dashboardResponses) {
                // Bandeau Pôle
                Row rowPole = sheetReport.createRow(rowIdx++);
                Cell cP = rowPole.createCell(0);
                cP.setCellValue("► " + r.pole.toUpperCase());
                cP.setCellStyle(styleEntete);
                rowPole.createCell(1).setCellStyle(styleEntete);
                rowPole.createCell(2).setCellStyle(styleEntete);

                // En-tête Charges
                Row rowHeader = sheetReport.createRow(rowIdx++);
                rowHeader.createCell(0).setCellValue("Répartition des Charges");
                rowHeader.getCell(0).setCellStyle(styleSousTitre);
                rowHeader.createCell(1).setCellValue("Montant (€)");
                rowHeader.getCell(1).setCellStyle(styleSousTitre);

                // Lignes de charges
                if (r.detailsCharges != null) {
                    for (Map.Entry<String, Double> entry : r.detailsCharges.entrySet()) {
                        Row rowCharge = sheetReport.createRow(rowIdx++);
                        rowCharge.createCell(0).setCellValue(entry.getKey());
                        Cell cM = rowCharge.createCell(1);
                        cM.setCellValue(entry.getValue());
                        cM.setCellStyle(styleMontant);
                    }
                }

                // Total Dépenses
                Row rowTotalDep = sheetReport.createRow(rowIdx++);
                Cell cTd1 = rowTotalDep.createCell(0);
                cTd1.setCellValue("TOTAL DÉPENSES " + r.pole.toUpperCase());
                cTd1.setCellStyle(styleTotal);
                Cell cTd2 = rowTotalDep.createCell(1);
                cTd2.setCellValue(r.depensesTotales);
                cTd2.setCellStyle(styleTotal);

                // Recettes
                Row rowRecettes = sheetReport.createRow(rowIdx++);
                rowRecettes.createCell(0).setCellValue("Total Recettes Réelles (Familles, CAF, etc.)");
                Cell cRecM = rowRecettes.createCell(1);
                cRecM.setCellValue(r.recettesTotales);
                cRecM.setCellStyle(styleMontant);

                // Indicateurs Clés
                Row rowTc = sheetReport.createRow(rowIdx++);
                rowTc.createCell(0).setCellValue("Taux de Couverture");
                Cell cTc = rowTc.createCell(1);
                cTc.setCellValue(r.tauxCouverture);
                cTc.setCellStyle(stylePourcentage);

                Row rowEcart = sheetReport.createRow(rowIdx++);
                rowEcart.createCell(0).setCellValue("Écart (Subvention Ville)");
                Cell cEcart = rowEcart.createCell(1);
                cEcart.setCellValue(r.ecart);
                cEcart.setCellStyle(styleMontant);

                Row rowCout = sheetReport.createRow(rowIdx++);
                rowCout.createCell(0).setCellValue("Coût Unitaire par enfant");
                Cell cCout = rowCout.createCell(1);
                cCout.setCellValue(r.coutUnitaire);
                cCout.setCellStyle(styleMontant);

                rowIdx += 2; // Espace entre les pôles
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
