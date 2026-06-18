$excel = New-Object -ComObject Excel.Application
$excel.Visible = $false
try {
    $workbook = $excel.Workbooks.Open("C:\Users\stagedg2\Projet_mairie_outil_tarification\VF_REC_DEP.xlsx")
    $sheet = $workbook.Sheets.Item("DEP")
    
    Write-Host "=== PREVIEW DEPENSES RE (Rows 2 to 27) ==="
    for ($r = 2; $r -le 27; $r++) {
        $rowStr = ""
        for ($c = 1; $c -le 15; $c++) {
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
    
    Write-Host "`n=== PREVIEW DEPENSES SC (Rows 28 to 50) ==="
    for ($r = 28; $r -le 50; $r++) {
        $rowStr = ""
        for ($c = 1; $c -le 15; $c++) {
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
} catch {
    Write-Error $_
} finally {
    $excel.Quit()
}
