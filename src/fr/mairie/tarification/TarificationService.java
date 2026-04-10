package fr.mairie.tarification;

import java.util.List;

/**
 * Service métier pour la recherche et le calcul des prix
 * basés sur le Quotient Familial (QF) de l'usager.
 */
public class TarificationService {

    /**
     * Recherche la tranche correspondante à un Quotient Familial donné.
     * 
     * @param qf Le quotient familial de l'usager.
     * @param grille La liste des tarifs de référence.
     * @return Le Tarif correspondant à la tranche de l'usager.
     * @throws Exception Si aucune tranche n'est trouvée pour ce QF.
     */
    public Tarif trouverTarif(double qf, List<Tarif> grille) throws Exception {
        for (Tarif t : grille) {
            // Un QF appartient à une tranche si : QF_MIN <= QF < QF_MAX
            if (qf >= t.getQfMin() && qf < t.getQfMax()) {
                return t;
            }
        }
        throw new Exception("Aucune tranche trouvée pour le QF : " + qf);
    }

    /**
     * Extrait le prix spécifique d'une activité.
     * 
     * @param t Le tarif de base.
     * @param activite La clé de l'activité (ex: REPAS, ADOS_VAC_JOURNEE_REPAS).
     * @return Le prix unitaire de l'activité.
     */
    public double obtenirPrix(Tarif t, String activite) {
        return t.getPrix(activite);
    }
}
