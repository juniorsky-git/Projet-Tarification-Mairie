$excel = New-Object -ComObject Excel.Application
$excel.Visible = $false
try {
    $workbook = $excel.Workbooks.Open("C:\Users\stagedg2\Projet_mairie_outil_tarification\VF_REC_DEP.xlsx")
    $sheet = $workbook.Sheets.Item("DEP")
    
    Write-Host "=== Row 29 Headers (SC) ==="
    $rowStr29 = ""
    for ($c = 1; $c -le 25; $c++) {
        $cell = $sheet.Cells.Item(29, $c)
        $val = "[EMPTY]"
        if ($cell.Value2 -ne $null) {
            $val = $cell.Value2.ToString()
        }
        $rowStr29 += "" + $c + ": " + $val + " | "
    }
    Write-Host $rowStr29

    Write-Host "=== Row 46 Totals (SC) ==="
    $rowStr46 = ""
    for ($c = 1; $c -le 25; $c++) {
        $cell = $sheet.Cells.Item(46, $c)
        $val = "[EMPTY]"
        if ($cell.Value2 -ne $null) {
            $val = $cell.Value2.ToString()
        }
        $rowStr46 += "" + $c + ": " + $val + " | "
    }
    Write-Host $rowStr46
} catch {
    Write-Error $_
} finally {
    $excel.Quit()
}
