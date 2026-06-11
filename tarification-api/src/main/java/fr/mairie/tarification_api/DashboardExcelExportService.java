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

    public byte[] exporterDashboard(Integer anneeActive, List<DepensePole> poles, Map<String, Double> recettesReelles, Map<String, Map<String, Double>> totauxSaisie, List<DashboardResponse> dashboardResponses, List<Map<String, Object>> comparatifData) throws IOException {
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
            
            CellStyle stylePredefini = workbook.createCellStyle();
            Font fontPredefini = workbook.createFont();
            fontPredefini.setBold(true);
            stylePredefini.setFont(fontPredefini);

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

            // ==========================================
            // FEUILLE 2 : COMPARATIF
            // ==========================================
            if (comparatifData != null && !comparatifData.isEmpty()) {
                Sheet sheetComp = workbook.createSheet("Comparatif " + anneeActive);
                sheetComp.setColumnWidth(0, 8000); // Pôle
                sheetComp.setColumnWidth(1, 4000); // Dép. Réf
                sheetComp.setColumnWidth(2, 4000); // Dép. Année
                sheetComp.setColumnWidth(3, 4000); // Écart Dép.
                sheetComp.setColumnWidth(4, 4000); // Rec. Réf
                sheetComp.setColumnWidth(5, 4000); // Rec. Année
                sheetComp.setColumnWidth(6, 4000); // Écart Rec.
                sheetComp.setColumnWidth(7, 3000); // TC Réf
                sheetComp.setColumnWidth(8, 3000); // TC Année

                // Styles spécifiques Comparatif
                CellStyle styleEcartPositif = workbook.createCellStyle();
                styleEcartPositif.cloneStyleFrom(styleMontant);
                Font fontPositif = workbook.createFont();
                fontPositif.setColor(IndexedColors.RED.getIndex());
                fontPositif.setBold(true);
                styleEcartPositif.setFont(fontPositif);

                CellStyle styleEcartNegatif = workbook.createCellStyle();
                styleEcartNegatif.cloneStyleFrom(styleMontant);
                Font fontNegatif = workbook.createFont();
                fontNegatif.setColor(IndexedColors.GREEN.getIndex());
                fontNegatif.setBold(true);
                styleEcartNegatif.setFont(fontNegatif);

                CellStyle styleEcartNeutre = workbook.createCellStyle();
                styleEcartNeutre.cloneStyleFrom(styleMontant);
                Font fontNeutre = workbook.createFont();
                fontNeutre.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
                styleEcartNeutre.setFont(fontNeutre);

                CellStyle stylePourcent = workbook.createCellStyle();
                stylePourcent.setDataFormat(workbook.createDataFormat().getFormat("0.0%"));

                Row rowTitreComp = sheetComp.createRow(0);
                Cell cellTitreComp = rowTitreComp.createCell(0);
                cellTitreComp.setCellValue("COMPARATIF ANNÉE PRÉCÉDENTE VS SAISIE " + anneeActive);
                cellTitreComp.setCellStyle(styleTitre);

                Row rowHeaderComp = sheetComp.createRow(2);
                String[] headersComp = {
                    "PÔLE", "DÉP. RÉF.", "DÉP. " + anneeActive, "ÉCART DÉP.", 
                    "REC. RÉF.", "REC. " + anneeActive, "ÉCART REC.", 
                    "TC RÉF.", "TC " + anneeActive
                };
                for (int i = 0; i < headersComp.length; i++) {
                    Cell c = rowHeaderComp.createCell(i);
                    c.setCellValue(headersComp[i]);
                    c.setCellStyle(styleEntete);
                }

                int rowIdxComp = 3;
                for (Map<String, Object> ligne : comparatifData) {
                    Row row = sheetComp.createRow(rowIdxComp++);
                    
                    Cell cPole = row.createCell(0);
                    cPole.setCellValue((String) ligne.get("pole"));
                    cPole.setCellStyle(stylePredefini);

                    row.createCell(1).setCellValue((Double) ligne.get("dep2025"));
                    row.getCell(1).setCellStyle(styleMontant);
                    
                    row.createCell(2).setCellValue((Double) ligne.get("depN"));
                    row.getCell(2).setCellStyle(styleMontant);

                    double ecartDep = (Double) ligne.get("ecartDep");
                    Cell cEcartDep = row.createCell(3);
                    cEcartDep.setCellValue(ecartDep);
                    cEcartDep.setCellStyle(ecartDep > 0 ? styleEcartPositif : (ecartDep < 0 ? styleEcartNegatif : styleEcartNeutre));

                    row.createCell(4).setCellValue((Double) ligne.get("rec2025"));
                    row.getCell(4).setCellStyle(styleMontant);
                    
                    row.createCell(5).setCellValue((Double) ligne.get("recN"));
                    row.getCell(5).setCellStyle(styleMontant);

                    double ecartRec = (Double) ligne.get("ecartRec");
                    Cell cEcartRec = row.createCell(6);
                    cEcartRec.setCellValue(ecartRec);
                    // Pour les recettes, un écart positif est "vert", négatif est "rouge"
                    cEcartRec.setCellStyle(ecartRec > 0 ? styleEcartNegatif : (ecartRec < 0 ? styleEcartPositif : styleEcartNeutre));

                    row.createCell(7).setCellValue((Double) ligne.get("tc2025"));
                    row.getCell(7).setCellStyle(stylePourcent);

                    row.createCell(8).setCellValue((Double) ligne.get("tcN"));
                    row.getCell(8).setCellStyle(stylePourcent);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
