package fr.mairie.tarification_api;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * Service d'analyse chirurgicale des facturations de fluides.
 * Calcule les écarts entre le facturé réel et la consommation théorique (M3/kWh).
 */
@Service
public class AnalytiqueFluideService {

    // Constantes de tarification (dérivées du BP 2025 réel)
    private static final double PRIX_EAU_M3 = 5.09;
    private static final double ABO_EAU_SEMESTRE = 10.67;

    private static final double PRIX_GAZ_M3 = 1.27;
    private static final double ABO_GAZ_ANNUEL = 5400.0; // Moyenne par gros site

    private static final double PRIX_ELEC_C4_KWH = 0.31; // Segment PME-PMI
    private static final double PRIX_ELEC_BLEU_KWH = 0.25; // Tarif régulé

    /**
     * Génère l'analyse détaillée pour les principaux sites.
     * Note : Dans une version finale, ces données viendraient d'une lecture 
     * en temps réel de l'Excel, ici nous utilisons les données consolidées.
     */
    public List<AnalytiqueFluide> genererAnalyseDetailed() {
        List<AnalytiqueFluide> analyses = new ArrayList<>();

        // --- GROUPE SCOLAIRE EUROPE (Exemple type avec gros volumes) ---
        analyses.add(calculer("GROUPE SCOLAIRE EUROPE", "Eau", 1086, 5530.99, "m3", PRIX_EAU_M3, ABO_EAU_SEMESTRE * 2));
        analyses.add(calculer("GROUPE SCOLAIRE EUROPE", "Gaz", 4386, 7059.97, "m3", PRIX_GAZ_M3, 1000.0)); // Abonnement estimé
        analyses.add(calculer("GROUPE SCOLAIRE EUROPE", "Electricité", 19761, 5066.07, "kWh", PRIX_ELEC_C4_KWH, 0));

        // --- STADE HENRI HURT ---
        analyses.add(calculer("STADE HENRI HURT", "Eau", 1283, 6924.97, "m3", PRIX_EAU_M3, ABO_EAU_SEMESTRE * 2));
        analyses.add(calculer("STADE HENRI HURT", "Electricité", 15987, 3862.31, "kWh", PRIX_ELEC_C4_KWH, 0));

        // --- MAISON DES ASSOCIATIONS ---
        analyses.add(calculer("MAISON DES ASSOCIATIONS", "Eau", 1086, 5530.99, "m3", PRIX_EAU_M3, ABO_EAU_SEMESTRE * 2));

        return analyses;
    }

    private AnalytiqueFluide calculer(String site, String fluide, double conso, double reel, String unite, double prixUnit, double fixe) {
        double theorique = (conso * prixUnit) + fixe;
        double delta = reel - theorique;
        double pourcentage = theorique > 0 ? (delta / theorique) * 100 : 0;
        
        // Alerte si l'écart dépasse 20% (suspicion de fuite ou erreur tarifaire)
        boolean alerte = Math.abs(pourcentage) > 20;

        return new AnalytiqueFluide(
            site, fluide, conso, unite, reel, theorique, delta, pourcentage, alerte
        );
    }
}
