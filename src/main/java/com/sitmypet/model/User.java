package com.sitmypet.model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private String photo;
    private String role;
    private boolean active;
    private String certificat;
    private LocalDateTime createdAt;

    public User() {}

    public User(int id, String nom, String prenom, String email) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
    }

    public User(String nom, String prenom, String email, String telephone, String adresse, String photo, String role, boolean active) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.photo = photo;
        this.role = role;
        this.active = active;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    
    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public String getCertificat() { return certificat; }
    public void setCertificat(String certificat) { this.certificat = certificat; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** Displayed in the ComboBox dropdown. */
    @Override
    public String toString() {
        return prenom + " " + nom + "  —  " + email;
    }
}
