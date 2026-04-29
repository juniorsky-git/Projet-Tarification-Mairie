package fr.mairie.tarification_api;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Gardien de la base de données.
 * Initialise un compte administrateur par défaut si la base est vide.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository repository;

    public DataInitializer(UtilisateurRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("💾 Vérification du compte administrateur...");
        Utilisateur admin = repository.findByUsername("admin").orElse(new Utilisateur());
        admin.setUsername("admin");
        admin.setPassword("admin"); 
        admin.setNom("Administrateur Système");
        admin.setRole("ADMIN");
        admin.setActif(true);
        repository.save(admin);
        System.out.println("✅ Compte 'admin' prêt et synchronisé (Mot de passe: admin)");
    }
}
