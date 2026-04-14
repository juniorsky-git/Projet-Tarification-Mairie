package fr.mairie.tarification;

import java.util.List;

/**
 * Service metier pour la recherche et le calcul des prix
 * bases sur le Quotient Familial (QF) de l'usager.
 * 
 * Ce service permet de faire la correspondance entre un QF saisi 
 * et la grille tarifaire officielle de la ville de Crosne.
 * 
 * @author Séri-khane YOLOU
 * @version 1.2
 */
public class TarificationService {

    /**
     * Recherche la tranche correspondante a un Quotient Familial donne.
     * 
     * @param qf Le quotient familial de l'usager.
     * @param grille La liste exhaustive des tarifs de reference (Grille 2025).
     * @return Le Tarif correspondant a la tranche de l'usager.
     * @throws Exception Si aucune tranche n'est trouvee pour ce QF.
     */
    public Tarif trouverTarif(double qf, List<Tarif> grille) throws Exception {
        for (Tarif t : grille) {
            // Un QF appartient a une tranche si : QF_MIN <= QF < QF_MAX
            if (qf >= t.getQfMin()) {
                if (qf < t.getQfMax()) {
                    return t;
                }
            }
        }
        throw new Exception("Aucune tranche trouvee pour le QF : " + qf);
    }

    /**
     * Methode deleguant la recuperation d'un prix unitaire pour une activite.
     * 
     * @param t Le tarif de la tranche identifiee.
     * @param activite La cle technique de l'activite (ex: REPAS, ETUDES_FORFAIT_MENSUEL).
     * @return Le prix unitaire de l'activite pour cette tranche.
     */
    public double obtenirPrix(Tarif t, String activite) {
        return t.getPrix(activite);
    }
}
