package fr.mairie.tarification_api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Configuration de la sécurité Web.
 * Pour l'instant, on laisse l'accès libre pour ne pas bloquer les tests,
 * mais la brique est prête pour le déploiement Web.
 */
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/*.css", "/*.js", "/*.ico", "/*.png", "/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().authenticated() 
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.sendError(401, "Session expirée");
                    } else {
                        response.sendRedirect("/");
                    }
                })
            )
            .formLogin(login -> login
                .loginPage("/") // On utilise la landing page comme point d'entrée
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll());
        
        return http.build();
    }

    /**
     * Pour l'instant on utilise pas de hachage complexe pour faciliter vos tests.
     * On passera à BCrypt lors du déploiement final sur Internet.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
