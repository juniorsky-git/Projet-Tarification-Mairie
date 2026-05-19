package fr.mairie.tarification_api;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Interface pour manipuler les utilisateurs dans PostgreSQL.
 */
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    
    /**
     * Recherche un utilisateur par son identifiant (username).
     * Très important pour la connexion.
     */
    Optional<Utilisateur> findByUsername(String username);
}
