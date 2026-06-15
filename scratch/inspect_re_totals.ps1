$excel = New-Object -ComObject Excel.Application
$excel.Visible = $false
try {
    $workbook = $excel.Workbooks.Open("C:\Users\stagedg2\Projet_mairie_outil_tarification\VF_REC_DEP.xlsx")
    $sheet = $workbook.Sheets.Item("DEP")
    
    Write-Host "=== Row 3 Headers ==="
    $rowStr3 = ""
    for ($c = 1; $c -le 25; $c++) {
        $cell = $sheet.Cells.Item(3, $c)
        $val = "[EMPTY]"
        if ($cell.Value2 -ne $null) {
            $val = $cell.Value2.ToString()
        }
        $rowStr3 += "" + $c + ": " + $val + " | "
    }
    Write-Host $rowStr3

    Write-Host "=== Row 21 Totals ==="
    $rowStr21 = ""
    for ($c = 1; $c -le 25; $c++) {
        $cell = $sheet.Cells.Item(21, $c)
        $val = "[EMPTY]"
        if ($cell.Value2 -ne $null) {
            $val = $cell.Value2.ToString()
        }
        $rowStr21 += "" + $c + ": " + $val + " | "
    }
    Write-Host $rowStr21
} catch {
    Write-Error $_
} finally {
    $excel.Quit()
}
