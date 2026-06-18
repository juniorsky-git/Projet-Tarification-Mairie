package fr.mairie.tarification_api;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité JPA représentant une ligne de saisie comptable manuelle.
 * 
 * Chaque ligne correspond à une charge (dépense) ou une recette
 * saisie par un agent pour une année donnée et un pôle donné.
 * 
 * Stockée dans la table PostgreSQL "saisie_comptable".
 * 
 * @author Stagiaire DG 2
 */
@Entity
@Table(name = "saisie_comptable", indexes = {
    @Index(name = "idx_saisie_annee_pole", columnList = "annee, pole"),
    @Index(name = "idx_saisie_type", columnList = "type_ligne")
})
public class LigneSaisie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Année de la saisie (ex: 2025, 2026) */
    @Column(nullable = false)
    private Integer annee;

    /** Nom du pôle (ex: "Restauration", "Accueil de Loisirs") */
    @Column(nullable = false, length = 100)
    private String pole;

    /**
     * Type de la ligne : "DEPENSE" ou "RECETTE"
     */
    @Column(name = "type_ligne", nullable = false, length = 20)
    private String typeLigne;

    /** Libellé de la ligne (ex: "Prestataire Scolarest", "Personnel") */
    @Column(nullable = false, length = 255)
    private String libelle;

    /** Montant en euros (peut être 0 si non encore renseigné) */
    @Column(nullable = false)
    private Double montant = 0.0;

    /**
     * Commentaire libre : anomalie, note explicative, etc.
     * ex: "Hausse exceptionnelle suite renouvellement contrat"
     */
    @Column(columnDefinition = "TEXT")
    private String commentaire;

    /**
     * Ligne prédéfinie par le système (non supprimable par l'agent).
     * Les lignes prédéfinies sont générées automatiquement à partir des données 2025.
     */
    @Column(nullable = false)
    private Boolean predefinie = false;

    /** Horodatage de création */
    @Column(name = "cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe = LocalDateTime.now();

    /** Horodatage de dernière modification */
    @Column(name = "modifie_le")
    private LocalDateTime modifieLe = LocalDateTime.now();

    // --- CONSTRUCTEURS ---

    public LigneSaisie() {}

    public LigneSaisie(Integer annee, String pole, String typeLigne,
                       String libelle, Double montant, Boolean predefinie) {
        this.annee = annee;
        this.pole = pole;
        this.typeLigne = typeLigne;
        this.libelle = libelle;
        this.montant = montant;
        this.predefinie = predefinie;
        this.creeLe = LocalDateTime.now();
        this.modifieLe = LocalDateTime.now();
    }

    // --- HOOK JPA : mise à jour de la date de modification ---
    @PreUpdate
    public void preUpdate() {
        this.modifieLe = LocalDateTime.now();
    }

    // --- ACCESSEURS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getAnnee() { return annee; }
    public void setAnnee(Integer annee) { this.annee = annee; }

    public String getPole() { return pole; }
    public void setPole(String pole) { this.pole = pole; }

    public String getTypeLigne() { return typeLigne; }
    public void setTypeLigne(String typeLigne) { this.typeLigne = typeLigne; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public Double getMontant() { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public Boolean getPredefinie() { return predefinie; }
    public void setPredefinie(Boolean predefinie) { this.predefinie = predefinie; }

    public LocalDateTime getCreeLe() { return creeLe; }
    public LocalDateTime getModifieLe() { return modifieLe; }
}
