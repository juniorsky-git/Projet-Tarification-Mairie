package fr.mairie.tarification;

import java.util.ArrayList;
import java.util.List;

/**
 * Entrepôt de données statiques pour les tarifs de référence.
 * Contient la grille tarifaire 2025 validée par la mairie.
 */
public class DonneesTarifs {

    /**
     * Charge l'intégralité des tarifs de référence pour 2025.
     * Cette grille est basée sur le document PDF officiel.
     * 
     * @return Une liste d'objets Tarif initialisés.
     */
    public static List<Tarif> chargerTarifsReference() {
        List<Tarif> grille = new ArrayList<>();

        // Format : Tranche, QF Min, QF Max, Prix Repas, Prix Garde
        grille.add(new Tarif("EXT", 18000, 10000000, 5.98, 0));
        grille.add(new Tarif("A",   18000, 10000000, 5.54, 0));
        grille.add(new Tarif("B",   15000, 18000, 4.89, 0));
        grille.add(new Tarif("B2",  13000, 15000, 4.32, 0));
        grille.add(new Tarif("C",   11000, 13000, 4.17, 0));
        grille.add(new Tarif("D",    9000, 11000, 3.51, 0));
        grille.add(new Tarif("E",    7000,  9000, 2.92, 0));
        grille.add(new Tarif("F",    5000,  7000, 2.16, 0));
        grille.add(new Tarif("F2",   3000,  5000, 1.57, 0));
        grille.add(new Tarif("G",       0,  3000, 1.43, 0));

        return grille;
    }
}
