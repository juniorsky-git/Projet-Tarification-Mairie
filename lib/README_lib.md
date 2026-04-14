# Dossier lib/ — Librairies Java

Ce dossier contient les JARs nécessaires à la compilation et à l'exécution du projet.
Il est exclu du dépôt Git (`.gitignore`) car les binaires ne doivent pas être versionnés.

## Comment recréer ce dossier

Copier les JARs suivants depuis SQL Developer (installé sur le poste) :

| JAR | Source |
|-----|--------|
| `pdfbox.jar` | `sqldeveloper/extensions/oracle.datamodeler/lib/` |
| `poi.jar` | `sqldeveloper/sqldeveloper/lib/` |
| `poi-ooxml.jar` | `sqldeveloper/sqldeveloper/lib/` |
| `poi-ooxml-lite.jar` | `sqldeveloper/sqldeveloper/lib/` |
| `commons-collections4.jar` | `sqldeveloper/sqldeveloper/lib/` |
| `commons-compress.jar` | `sqldeveloper/sqldeveloper/lib/` |
| `commons-io.jar` | `sqldeveloper/sqldeveloper/lib/` |
| `log4j-api.jar` | `sqldeveloper/sqldeveloper/lib/` |
| `log4j-core.jar` | `sqldeveloper/sqldeveloper/lib/` |
| `slf4j-api.jar` | `sqldeveloper/sqldeveloper/lib/` |
| `SparseBitSet.jar` | `sqldeveloper/sqldeveloper/lib/` |
| `xmlbeans.jar` | `sqldeveloper/sqldeveloper/lib/` |

## Compilation

```powershell
$Javac = "C:\Users\Junior\Desktop\sqldeveloper\jdk\jre\bin\javac.exe"
$Libs  = (Get-ChildItem lib\*.jar | ForEach-Object { $_.FullName }) -join ";"
& $Javac -encoding UTF-8 -d build -cp "build;$Libs" -sourcepath src (Get-ChildItem src\fr\mairie\tarification\*.java)
```
