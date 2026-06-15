$excel = New-Object -ComObject Excel.Application
$excel.Visible = $false
try {
    $workbook = $excel.Workbooks.Open("C:\Users\stagedg2\Projet_mairie_outil_tarification\VF_REC_DEP.xlsx")
    $sheet = $workbook.Sheets.Item("DEP")
    Write-Host "Recherche de 'DEPENSES CL' dans la feuille DEP..."
    $range = $sheet.UsedRange
    $found = $range.Find("DEPENSES CL")
    if ($found -ne $null) {
        Write-Host "Trouvé à $($found.Address()) (Row: $($found.Row), Col: $($found.Column))"
        # Print 20 rows starting from this one
        for ($i = -1; $i -le 20; $i++) {
            $rowStr = ""
            for ($j = 0; $j -le 10; $j++) {
                $cell = $sheet.Cells.Item($found.Row + $i, $found.Column + $j)
                if ($cell.Value2 -ne $null) {
                    $rowStr += $cell.Value2.ToString() + "`t"
                } else {
                    $rowStr += "[EMPTY]`t"
                }
            }
            Write-Host "Row $($found.Row + $i) : $rowStr"
        }
    } else {
        Write-Host "Non trouvé."
    }
} catch {
    Write-Error $_
} finally {
    $excel.Quit()
}
