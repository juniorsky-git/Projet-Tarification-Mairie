package fr.mairie.tarification_api;

import java.util.List;
import java.util.Map;

public class DonneesBudgetaires {

    public static final List<DepensePole> POLES = List.of(
        new DepensePole(
            "Restauration",
            1_813_105.51,
            11.48,
            1128,    // nombreEnfants
            157920,  // unitesAnnuelles (Volume repas)
            Map.of(
                "Alimentation", 715_204.38,
                "Fluides", 46_881.49,
                "Materiel et fournitures", 0.0,
                "Sorties", 0.0,
                "Transport", 0.0,
                "Hebergement", 0.0,
                "Personnel", 1_051_019.64,
                "Autres", 0.0
            ),
            Map.of(
                "EXT", 11,
                "A", 429,
                "B", 90,
                "B2", 90,
                "C", 79,
                "D", 79,
                "E", 90,
                "F", 79,
                "F2", 57,
                "G", 124
            )
        ),
        new DepensePole(
            "Accueil de Loisirs",
            1_596_116.14,
            5868.07,
            272,     // nombreEnfants
            null,    // Pas d'unités annuelles spécifiques (coût par enfant)
            Map.of(
                "Alimentation", 63_094.70,
                "Fluides", 43_486.21,
                "Materiel et fournitures", 17_446.99,
                "Sorties", 23_735.97,
                "Transport", 14_552.99,
                "Hebergement", 0.0,
                "Personnel", 1_428_849.37,
                "Autres", 4_949.91
            )
        ),
        new DepensePole(
            "Accueil periscolaire",
            658_856.78,
            582.54,
            1131,    // nombreEnfants
            null,
            Map.of(
                "Alimentation", 5_810.47,
                "Fluides", 55_068.93,
                "Materiel et fournitures", 0.0,
                "Sorties", 0.0,
                "Transport", 0.0,
                "Hebergement", 0.0,
                "Personnel", 597_977.38,
                "Autres", 0.0
            ) 
        ),
        new DepensePole(
            "Etudes surveillees",
            58_598.14,
            353.00,
            166,     // nombreEnfants
            null,
            Map.of(
                "Alimentation", 0.0,
                "Fluides", 0.0,
                "Materiel et fournitures", 0.0,
                "Sorties", 0.0,
                "Transport", 0.0,
                "Hebergement", 0.0,
                "Personnel", 58_598.14,
                "Autres", 0.0
            )
        ),
        new DepensePole(
            "Espace Ados",
            140_775.54,
            1268.25,
            111,     // nombreEnfants
            null,
            Map.of(
                "Alimentation", 59.24,
                "Fluides", 14_844.25,
                "Materiel et fournitures", 1_733.73,
                "Sorties", 0.0,
                "Transport", 4_016.42,
                "Hebergement", 0.0,
                "Personnel", 119_794.89,
                "Autres", 327.01
            )
        ),
        new DepensePole(
            "Sejours",
            107_127.71,
            2021.28,
            53,      // nombreEnfants
            null,
            Map.of(
                "Alimentation", 355.46,
                "Fluides", 0.0,
                "Materiel et fournitures", 382.75,
                "Sorties", 0.0,
                "Transport", 45_304.83,
                "Hebergement", 60_769.94,
                "Personnel", 0.0,
                "Autres", 314.73
            )
        )
    );
}
