package fr.mairie.tarification_api;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

/**
 * Service faisant le lien entre Spring Security et notre base de données PostgreSQL.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository repository;

    public CustomUserDetailsService(UtilisateurRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur user = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + username));

        return new User(
            user.getUsername(),
            user.getPassword(),
            new ArrayList<>() // On pourra ajouter les rôles (ADMIN/AGENT) ici plus tard
        );
    }
}
