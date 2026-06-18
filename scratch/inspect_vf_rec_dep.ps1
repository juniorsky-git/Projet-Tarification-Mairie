$excel = New-Object -ComObject Excel.Application
$excel.Visible = $false
try {
    $workbook = $excel.Workbooks.Open("C:\Users\stagedg2\Projet_mairie_outil_tarification\VF_REC_DEP.xlsx")
    Write-Host "Sheets in VF_REC_DEP.xlsx:"
    foreach ($sheet in $workbook.Sheets) {
        Write-Host " - Sheet: $($sheet.Name)"
        $range = $sheet.UsedRange
        # Print first 20 rows and first 10 columns
        Write-Host "Preview of first 20 rows of $($sheet.Name):"
        for ($r = 1; $r -le 25; $r++) {
            $rowStr = ""
            for ($c = 1; $c -le 12; $c++) {
                $cell = $sheet.Cells.Item($r, $c)
                if ($cell.Value2 -ne $null) {
                    $rowStr += $cell.Value2.ToString() + "`t"
                } else {
                    $rowStr += "[EMPTY]`t"
                }
            }
            if ($rowStr.Trim().Replace("[EMPTY]", "").Trim() -ne "") {
                Write-Host "Row $r : $rowStr"
            }
        }
    }
} catch {
    Write-Error $_
} finally {
    $excel.Quit()
}
