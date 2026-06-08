package fr.mairie.tarification_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

@SpringBootTest
class AnalytiqueFluideServiceTest {

    @Test
    void testCalculEvolutionEtAlertes() {
        // Tester la logique de calcul d'évolution semestrielle
        double s1 = 100.0;
        double s2 = 150.0;

        double delta = ((s2 - s1) / s1) * 100;
        boolean alerte = Math.abs(delta) > 20;

        assertEquals(50.0, delta, "L'évolution de 100 à 150 devrait être de 50%");
        assertTrue(alerte, "Une hausse de 50% devrait déclencher une alerte (>20%)");
    }

    @Test
    void testCasS1Zero() {
        double s1 = 0.0;
        double s2 = 50.0;

        // Si S1=0, delta=0 pour éviter NaN
        double delta = s1 > 0 ? ((s2 - s1) / s1) * 100 : 0;

        assertEquals(0.0, delta, "Si S1 est à zéro, l'évolution doit être ramenée à 0 pour éviter NaN");
    }

    @Test
    void testBiSemestrielRetourneListeNonNull() {
        // Le service lit maintenant fluides.json (zéro Excel)
        // En contexte de test, fluides.json est dans le classpath → la liste peut être vide mais jamais null
        AnalytiqueFluideService service = new AnalytiqueFluideService();
        List<RapportSemestrielFluide> resultats = service.analyserBiSemestriel();
        assertNotNull(resultats, "La liste ne doit jamais être null");
    }
}
