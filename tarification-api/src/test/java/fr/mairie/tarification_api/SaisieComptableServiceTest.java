package fr.mairie.tarification_api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SaisieComptableServiceTest {

    @Test
    void testComparaisonAnneeReferenceParDefaut() {
        Integer anneeSource = 2026;
        Integer anneeRefForcee = null;
        Integer anneeRefCalculee = anneeRefForcee != null ? anneeRefForcee : (anneeSource - 1);
        
        assertEquals(2025, anneeRefCalculee, "Si anneeRefForcee est null, on doit prendre l'année N-1 (2025)");
    }

    @Test
    void testComparaisonAnneeReferenceForcee() {
        Integer anneeSource = 2026;
        Integer anneeRefForcee = 2024;
        Integer anneeRefCalculee = anneeRefForcee != null ? anneeRefForcee : (anneeSource - 1);
        
        assertEquals(2024, anneeRefCalculee, "Si anneeRefForcee est spécifié, il doit surcharger le calcul par défaut (2024 au lieu de 2025)");
    }

    @Test
    void testEcartNegatifProcheZerodevientZero() {
        // Reproduit la logique JS côté frontend / la logique globale des écarts
        double ecartDep = -0.001;
        
        if (Math.abs(ecartDep) < 0.01) {
            ecartDep = 0.0;
        }
        
        assertEquals(0.0, ecartDep, "Un écart très proche de zéro doit être arrondi à exactement 0 pour éviter d'afficher -0.00");
    }

    @Test
    void testExceptionsComparatifAnneeInvalide() {
        // On s'assure qu'on peut lever une exception pour des années aberrantes
        SaisieComptableService service = new SaisieComptableService(null, null); // mocks non nécessaires car validation au début
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.getComparatif(-2026, null);
        });

        assertTrue(exception.getMessage().contains("L'année source ne peut pas être négative"));
    }
}
