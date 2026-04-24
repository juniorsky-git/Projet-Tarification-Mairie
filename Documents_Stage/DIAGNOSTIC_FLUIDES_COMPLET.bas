Attribute VB_Name = "DiagnosticCompletFluides"
' ---------------------------------------------------------------------------------------
' SCRIPT D'AUDIT ÉNERGÉTIQUE - VILLE DE CROSNE (STAGE 2026)
' Objectif : Validation croisée des résultats du Dashboard Java par audit Excel VBA.
' ---------------------------------------------------------------------------------------

Option Explicit

' --- Constantes de prix (IDENTIQUES AU CODE JAVA) ---
Private Const PRIX_ELEC As Double = 0.31
Private Const PRIX_GAZ As Double = 1.21
Private Const PRIX_EAU As Double = 4.5

Sub GenererRapportDiagnostic()
    Dim wsRepo As Worksheet
    Dim lastRow As Long, i As Long, j As Long, nextLine As Long
    Dim site As String, conso As Double, reel As Double, theorique As Double, ecart As Double
    Dim sheetsToAnalize As Variant, currentSheet As Variant
    
    ' 1. Création ou Nettoyage de la feuille de rapport
    On Error Resume Next
    Set wsRepo = ThisWorkbook.Worksheets("REPORT_DIAGNOSTIC")
    On Error GoTo 0
    
    If wsRepo Is Nothing Then
        Set wsRepo = ThisWorkbook.Worksheets.Add(After:=ThisWorkbook.Worksheets(ThisWorkbook.Worksheets.Count))
        wsRepo.Name = "REPORT_DIAGNOSTIC"
    Else
        wsRepo.Cells.Clear
    End If
    
    ' 2. En-tête du rapport
    With wsRepo
        .Range("A1").Value = "RAPPORT D'AUDIT FINANCIER ÉNERGÉTIQUE (2025)"
        .Range("A1:F1").Merge
        .Range("A1").Font.Bold = True
        .Range("A1").Font.Size = 14
        .Range("A3:F3").Value = Array("SITE", "FLUIDE", "CONSOMMATION", "THÉORIQUE (€)", "RÉEL (€)", "ÉCART (%)")
        .Range("A3:F3").Interior.Color = RGB(50, 50, 50)
        .Range("A3:F3").Font.Color = RGB(255, 255, 255)
        nextLine = 4
    End With
    
    ' --- MODIFICATION : ON SE CONCENTRE UNIQUEMENT SUR L'ÉLECTRICITÉ ---
    sheetsToAnalize = Array("CONSO ELEC")
    
    ' 3. Analyse
    For Each currentSheet In sheetsToAnalize
        If SheetExists(CStr(currentSheet)) Then
            Dim ws As Worksheet
            Set ws = ThisWorkbook.Worksheets(CStr(currentSheet))
            
            ' On commence à la ligne 10 jusqu'à la fin
            lastRow = ws.Cells(ws.Rows.Count, 6).End(xlUp).Row
            
            For i = 10 To lastRow
                site = ws.Cells(i, 6).Value
                
                ' On ignore les totaux
                If site <> "" And InStr(1, site, "TOTAL", vbTextCompare) = 0 Then
                    conso = 0
                    reel = 0
                    
                    ' SCAN : on cherche le motif de l'année "25"
                    ' On utilise un Pas de 3 colonnes comme dans le code Java
                    For j = 14 To 150 Step 3
                        Dim cellVal As Variant
                        cellVal = ws.Cells(i, j).Value
                        
                        If Not IsError(cellVal) Then
                            If InStr(1, CStr(cellVal), "25", vbTextCompare) > 0 Then
                                ' Conso est 2 colonnes plus loin (j+2)
                                ' Montant est 3 colonnes plus loin (j+3)
                                If IsNumeric(ws.Cells(i, j + 2).Value) Then
                                    conso = conso + ws.Cells(i, j + 2).Value
                                End If
                                If IsNumeric(ws.Cells(i, j + 3).Value) Then
                                    reel = reel + ws.Cells(i, j + 3).Value
                                End If
                            End If
                        End If
                    Next j
                    
                    ' Si on a trouvé de la conso, on affiche
                    If conso > 0 Then
                        theorique = conso * PRIX_ELEC
                        ecart = (reel - theorique) / theorique
                        
                        wsRepo.Cells(nextLine, 1).Value = site
                        wsRepo.Cells(nextLine, 2).Value = "ÉLECTRICITÉ"
                        wsRepo.Cells(nextLine, 3).Value = conso
                        wsRepo.Cells(nextLine, 4).Value = theorique
                        wsRepo.Cells(nextLine, 5).Value = reel
                        wsRepo.Cells(nextLine, 6).Value = ecart
                        wsRepo.Cells(nextLine, 6).NumberFormat = "0.0%"
                        
                        ' Couleur
                        If Abs(ecart) > 0.5 Then wsRepo.Rows(nextLine).Font.Color = RGB(200, 0, 0)
                        
                        nextLine = nextLine + 1
                    End If
                End If
            Next i
        Else
            MsgBox "Attention : La feuille '" & currentSheet & "' n'existe pas dans ce classeur."
        End If
    Next currentSheet
    
    wsRepo.Columns("A:F").AutoFit
    MsgBox "Analyse terminée ! Le rapport a été généré dans 'REPORT_DIAGNOSTIC'.", vbInformation
End Sub

Function SheetExists(shtName As String) As Boolean
    Dim st As Worksheet
    On Error Resume Next
    Set st = ThisWorkbook.Worksheets(shtName)
    On Error GoTo 0
    SheetExists = Not st Is Nothing
End Function
