# Script de construction pour le projet de tarification

$Action = $args[0]
if ($null -eq $Action) { $Action = "jar" }

# Chemins - Detection automatique de la version Java installee (evite le bug lors des MAJ)
$ExtensionsPath = "C:\Users\stagedg2\.antigravity\extensions"
$JavaExtension = Get-ChildItem $ExtensionsPath -Filter "redhat.java-*-win32-x64" -Directory | Sort-Object Name -Descending | Select-Object -First 1
$JavaBin = "$($JavaExtension.FullName)\jre\21.0.10-win32-x86_64\bin"
$Javac = "$JavaBin\javac.exe"
$Java  = "$JavaBin\java.exe"
$Jar   = "$JavaBin\jar.exe"

$SrcPath = "src/fr/mairie/tarification/"
$BuildPath = "build"
$DistPath = "dist"

# Librairies
$Libs = (Get-ChildItem lib\*.jar | ForEach-Object { $_.FullName }) -join ";"

function Build-Project {
    if (!(Test-Path $BuildPath)) { New-Item -ItemType Directory $BuildPath | Out-Null }
    Write-Host "--- Compilation en cours... ---" -ForegroundColor Cyan
    & $Javac -encoding UTF-8 -d $BuildPath -cp "$BuildPath;$Libs" -sourcepath src (Get-ChildItem $SrcPath/*.java)
    if ($LASTEXITCODE -eq 0) { Write-Host "Compilation réussie !" -ForegroundColor Green }
    else { Write-Host "Erreur de compilation." -ForegroundColor Red; exit 1 }
}

function Create-Jar {
    Build-Project
    if (!(Test-Path $DistPath)) { New-Item -ItemType Directory $DistPath | Out-Null }
    Write-Host "--- Création du JAR... ---" -ForegroundColor Cyan
    & $Jar cfe "$DistPath/tarification.jar" fr.mairie.tarification.Main -C $BuildPath fr
    Write-Host "JAR créé dans $DistPath/tarification.jar" -ForegroundColor Green
}

function Run-Project {
    Build-Project
    Write-Host "--- Lancement de l'application... ---" -ForegroundColor Cyan
    & $Java -cp "$BuildPath;$Libs" fr.mairie.tarification.Main
}

function Clean-Project {
    Write-Host "--- Nettoyage... ---" -ForegroundColor Yellow
    if (Test-Path $BuildPath) { Remove-Item -Recurse -Force $BuildPath }
    if (Test-Path $DistPath) { Remove-Item -Recurse -Force $DistPath }
    Write-Host "Dossiers build/ et dist/ supprimés." -ForegroundColor Green
}

switch ($Action) {
    "all"   { Create-Jar }
    "build" { Build-Project }
    "jar"   { Create-Jar }
    "run"   { Run-Project }
    "clean" { Clean-Project }
    default {
        Write-Host "Usage: ./build.ps1 [build | run | jar | clean]" -ForegroundColor Cyan
    }
}
