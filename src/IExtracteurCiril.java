import java.util.List;

/**
 * Interface commune à tous les extracteurs de fichiers Ciril.
 * Chaque type de fichier de la mairie (Restauration, Loisirs, etc.)
 * aura sa propre implémentation de cette interface.
 */
public interface IExtracteurCiril {

    /**
     * Charge et retourne la liste des tranches tarifaires
     * extraites depuis le fichier Excel passé en paramètre.
     *
     * @param cheminFichier Chemin absolu ou relatif vers le fichier .xlsx
     * @return Liste des objets Tarif construits depuis le fichier
     */
    List<Tarif> chargerTarifs(String cheminFichier);
}
