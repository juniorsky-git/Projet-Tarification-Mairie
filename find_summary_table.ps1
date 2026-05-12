$excel = New-Object -ComObject Excel.Application
$excel.Visible = $false
$workbook = $excel.Workbooks.Open("C:\Users\stagedg2\Projet_mairie_outil_tarification\tarification-api\Depenses recettes nf.xlsx")
foreach ($sheet in $workbook.Sheets) {
    Write-Host "Recherche dans $($sheet.Name)..."
    $range = $sheet.UsedRange
    $found = $range.Find("Tableau synthetique des recettes")
    if ($found -ne $null) {
        Write-Host "Trouvé dans $($sheet.Name) à la cellule $($found.Address())"
        for ($i=0; $i -le 5; $i++) {
            $rowStr = ""
            for ($j=0; $j -le 10; $j++) {
                $cell = $sheet.Cells.Item($found.Row + $i, $found.Column + $j)
                if ($cell.Value2 -ne $null) {
                    $rowStr += $cell.Value2.ToString() + " | "
                } else {
                    $rowStr += "NULL | "
                }
            }
            Write-Host $rowStr
        }
    }
}
$excel.Quit()
