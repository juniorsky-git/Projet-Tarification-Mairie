package fr.mairie.tarification_api;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entité représentant un utilisateur (agent municipal) du système.
 * Stocké dans la base PostgreSQL pour la gestion des accès Web.
 */
@Entity
@Table(name = "utilisateurs")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // Sera haché via BCrypt

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String role; // ex: "ADMIN", "AGENT"

    private boolean actif = true;

    // --- ACCESSEURS (GETTERS/SETTERS) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public Utilisateur() {}
}
