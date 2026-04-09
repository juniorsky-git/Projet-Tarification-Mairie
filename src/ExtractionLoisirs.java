import java.util.ArrayList;
import java.util.List;

/**
 * Extracteur spécialisé pour les futurs fichiers d'export Ciril
 * de type "Accueil de Loisirs" (périscolaire, mercredis, vacances...).
 *
 * Structure attendue du fichier (à adapter quand on reçoit le vrai fichier) :
 *  - Col A : libellé de la tranche QF
 *  - Col B : lettre de la tranche
 *  - Col C : prix de la journée
 *  - Col D : nombre d'enfants inscrits
 *  - Col F : dépenses annuelles
 *  - Col G : recettes annuelles
 *
 * NOTE : Cette classe est un squelette prêt à compléter dès réception
 * du fichier Ciril "Loisirs" de la mairie.
 */
public class ExtractionLoisirs implements IExtracteurCiril {

    @Override
    public List<Tarif> chargerTarifs(String cheminFichier) {
        List<Tarif> tarifs = new ArrayList<>();

        // TODO : adapter la structure ci-dessous quand on disposera
        // du vrai format du fichier Loisirs exporté depuis Ciril.
        //
        // Exemple : si les données commencent à la ligne 6 (index 5)
        // et que les recettes sont en colonne H (index 7) au lieu de G (index 6),
        // il suffira de changer les numéros de ligne/colonne ici,
        // sans toucher au reste du code.

        System.out.println("[ExtractionLoisirs] Extracteur en attente du fichier Ciril 'Loisirs'.");
        return tarifs;
    }
}
