$excel = New-Object -ComObject Excel.Application
$excel.Visible = $false
try {
    $workbook = $excel.Workbooks.Open("C:\Users\stagedg2\Projet_mairie_outil_tarification\VF_REC_DEP.xlsx")
    $sheet = $workbook.Sheets.Item("DEP")
    Write-Host "Scanning sheet DEP for any header cells containing 'DEPENSES' or similar..."
    $range = $sheet.UsedRange
    for ($r = 1; $r -le $range.Rows.Count; $r++) {
        $cellVal = $sheet.Cells.Item($r, 2).Value2
        if ($cellVal -ne $null -and $cellVal.ToString().Contains("DEPENSES")) {
            Write-Host "Found '$cellVal' at Row $r"
        }
    }
} catch {
    Write-Error $_
} finally {
    $excel.Quit()
}
