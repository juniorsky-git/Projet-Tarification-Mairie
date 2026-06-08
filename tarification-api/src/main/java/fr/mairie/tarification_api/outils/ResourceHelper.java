package fr.mairie.tarification_api.outils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.springframework.core.io.ClassPathResource;

public class ResourceHelper {
    public static InputStream getExcelInputStream(String nomFichier) throws Exception {
        // 1. Essayer le chemin tel quel
        File file = new File(nomFichier);
        if (file.exists()) return new FileInputStream(file);
        
        // 2. Essayer avec tarification-api/
        file = new File("tarification-api/" + nomFichier);
        if (file.exists()) return new FileInputStream(file);
        
        // 3. Essayer avec Donnees/Autres/
        file = new File("Donnees/Autres/" + nomFichier);
        if (file.exists()) return new FileInputStream(file);
        
        // 4. Essayer avec src/main/resources/
        file = new File("src/main/resources/" + nomFichier);
        if (file.exists()) return new FileInputStream(file);

        // 5. Sur Railway (dans le JAR), chercher dans le classpath
        // Le nom de fichier peut avoir un chemin (ex: Donnees/Autres/CALC.xlsx)
        // On récupère juste le nom du fichier pour chercher à la racine du classpath
        String baseName = new File(nomFichier).getName();
        ClassPathResource cpr = new ClassPathResource(baseName);
        if (cpr.exists()) return cpr.getInputStream();
        
        // 6. Essayer avec le chemin complet dans le classpath
        cpr = new ClassPathResource(nomFichier);
        if (cpr.exists()) return cpr.getInputStream();
        
        throw new java.io.FileNotFoundException("Fichier introuvable sur le disque ni dans le classpath: " + nomFichier);
    }
}
