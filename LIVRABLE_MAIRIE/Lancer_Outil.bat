@echo off
title Outil de Tarification Mairie - Serveur Local
echo ======================================================
echo    LANCEMENT DE L'OUTIL DE TARIFICATION MAIRIE
echo ======================================================
echo.
echo [INFO] Demarrage du serveur...
echo [INFO] Ne fermez pas cette fenetre pendant l'utilisation.
echo.
echo [INFO] Acces au site : http://localhost:8080
echo.
java -jar tarification-api.jar
echo.
echo [ATTENTION] Le serveur s'est arrete.
pause
