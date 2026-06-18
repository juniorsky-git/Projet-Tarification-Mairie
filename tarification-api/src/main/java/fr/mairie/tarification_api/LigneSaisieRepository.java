package fr.mairie.tarification_api;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository JPA pour les lignes de saisie comptable.
 * 
 * Fournit les opérations CRUD standard + requêtes métier spécifiques
 * pour la gestion des données comptables par année et par pôle.
 * 
 * @author Stagiaire DG 2
 */
@Repository
public interface LigneSaisieRepository extends JpaRepository<LigneSaisie, Long> {

    /**
     * Récupère toutes les lignes d'une année donnée, triées par pôle puis libellé.
     * @param annee L'année à consulter (ex: 2025, 2026)
     */
    List<LigneSaisie> findByAnneeOrderByPoleAscLibelleAsc(Integer annee);

    /**
     * Récupère toutes les lignes d'un pôle pour une année donnée.
     * @param annee L'année.
     * @param pole Le nom du pôle.
     */
    List<LigneSaisie> findByAnneeAndPoleOrderByTypeLigneAscLibelleAsc(Integer annee, String pole);

    /**
     * Récupère les dépenses d'un pôle pour une année.
     */
    List<LigneSaisie> findByAnneeAndPoleAndTypeLigne(Integer annee, String pole, String typeLigne);

    /**
     * Vérifie si des données ont été saisies pour une année donnée.
     * @param annee L'année à vérifier.
     */
    boolean existsByAnnee(Integer annee);

    /**
     * Supprime toutes les lignes d'une année pour un pôle (utile pour la réinitialisation).
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM LigneSaisie l WHERE l.annee = :annee AND l.pole = :pole")
    void deleteByAnneeAndPole(@Param("annee") Integer annee, @Param("pole") String pole);

    /**
     * Calcule la somme des dépenses réelles pour un pôle et une année.
     * Retourne 0.0 si aucune donnée n'est présente.
     */
    @Query("SELECT COALESCE(SUM(l.montant), 0.0) FROM LigneSaisie l " +
           "WHERE l.annee = :annee AND l.pole = :pole AND l.typeLigne = 'DEPENSE'")
    Double sumDepensesByAnneeAndPole(@Param("annee") Integer annee, @Param("pole") String pole);

    /**
     * Calcule la somme des recettes réelles pour un pôle et une année.
     */
    @Query("SELECT COALESCE(SUM(l.montant), 0.0) FROM LigneSaisie l " +
           "WHERE l.annee = :annee AND l.pole = :pole AND l.typeLigne = 'RECETTE'")
    Double sumRecettesByAnneeAndPole(@Param("annee") Integer annee, @Param("pole") String pole);

    /**
     * Liste toutes les années distinctes ayant des données saisies (tri décroissant).
     */
    @Query("SELECT DISTINCT l.annee FROM LigneSaisie l ORDER BY l.annee DESC")
    List<Integer> findDistinctAnnees();
}
