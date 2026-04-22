# Script de configuration de l'environnement Dev Mairie
Write-Host "--- Initialisation de l'environnement Hybride ---" -ForegroundColor Cyan

# 1. Démarrage de PostgreSQL via Docker
Write-Host "[1/3] Démarrage de la base de données..." -ForegroundColor Yellow
docker-compose up -d

# 2. Branchement sur le JDK Local (Eclipse Adoptium)
Write-Host "[2/3] Configuration du JDK (Java)..." -ForegroundColor Yellow
$jdkPath = "C:\Users\stagedg2\AppData\Local\Programs\Eclipse Adoptium\jdk-25.0.2.10-hotspot\bin"
if (Test-Path $jdkPath) {
    $env:Path = "$jdkPath;" + $env:Path
    Write-Host "JDK 25 activé avec succès." -ForegroundColor Green
} else {
    Write-Host "ATTENTION: JDK introuvable à l'adresse habituelle. Merci de vérifier." -ForegroundColor Red
}

# 3. Création d'un raccourci (alias) pour PostgreSQL
# Plus besoin d'installer psql sur Windows, on utilise celui du conteneur !
function psql-mairie {
    docker exec -it mairie-db psql -U admin -d mairie_tarifs
}
Set-Alias -Name psqlm -Value psql-mairie

Write-Host "`n--- Environnement Prêt ! ---" -ForegroundColor Cyan
Write-Host "Vérifications :"
java -version
Write-Host "`nTape 'psqlm' pour te connecter à ta base de données." -ForegroundColor DarkCyan
