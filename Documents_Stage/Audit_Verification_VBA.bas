Attribute VB_Name = "ModuleAuditFluides"

' =========================================================================
' OUTIL D'AUDIT INTERNE - VERIFICATION CROISEE (JAVA vs VBA)
' Objectif : Valider le cumul annuel 2025 pour l'Ã©lectricitÃ©
' DÃ©veloppÃ© dans le cadre du stage Mairie de Crosne (Issue #29)
' =========================================================================

Sub CalculBilan2025_Audit()
    Dim ws As Worksheet
    Dim r As Long, col As Integer
    Dim totalKwh As Double, totalEuros As Double
    Dim cellVal As String
    Dim siteTrouve As Boolean
    
    Set ws = ThisWorkbook.Sheets("CONSO ELEC")
    totalKwh = 0: totalEuros = 0: siteTrouve = False
    
    ' On cherche le site auditÃ©
    For r = 9 To ws.UsedRange.Rows.Count
        If InStr(1, ws.Cells(r, 6).Value, "RESTOS", vbTextCompare) > 0 Then
            siteTrouve = True
            ' On scanne chaque cellule de la ligne
            For col = 7 To 150
                cellVal = CStr(ws.Cells(r, col).Value)
                ' DÃ©tection du motif 2025
                If Left(LCase(Trim(cellVal)), 2) = "du" And InStr(1, cellVal, "25") > 0 Then
                    If IsNumeric(ws.Cells(r, col + 2).Value) Then totalKwh = totalKwh + ws.Cells(r, col + 2).Value
                    If IsNumeric(ws.Cells(r, col + 3).Value) Then totalEuros = totalEuros + ws.Cells(r, col + 3).Value
                    col = col + 3
                End If
            Next col
            Exit For
        End If
    Next r
    
    If siteTrouve Then
        MsgBox "RESULTAT AUDIT VBA :" & vbCrLf & "kWh : " & totalKwh & vbCrLf & "TTC : " & totalEuros & " â‚¬"
    End If
End Sub
