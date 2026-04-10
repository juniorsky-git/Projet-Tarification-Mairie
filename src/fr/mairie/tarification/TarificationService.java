package fr.mairie.tarification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TarificationService {

    public Tarif trouverTarif(double qf, List<Tarif> tarifs) {
        if (qf < 0) {
            throw new IllegalArgumentException("Le QF ne peut pas être négatif.");
        }
        for (Tarif tarif : tarifs) {
            if (tarif.contientQf(qf)) {
                return tarif;
            }
        }
        throw new IllegalArgumentException("Aucune tranche trouvée pour le QF : " + qf);
    }

    public double obtenirPrix(Tarif tarif, String activite) {
        if (tarif == null) {
            throw new IllegalArgumentException("Aucun tarif trouvé.");
        }
        if (!tarif.aActivite(activite.toLowerCase())) {
            throw new IllegalArgumentException(
                "Activité inconnue ou non disponible pour cette tranche : " + activite +
                "\nActivités disponibles : " + tarif.getActivitesDisponibles()
            );
        }
        return tarif.getPrix(activite);
    }
}
