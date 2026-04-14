package model;

import java.time.LocalDateTime;

public class Reponse {
    private int id;
    private String contenu;
    private LocalDateTime dateReponse;
    private String auteur;
    private int reclamationId;

    public Reponse() {}

    public Reponse(String contenu, LocalDateTime dateReponse, String auteur, int reclamationId) {
        this.contenu = contenu;
        this.dateReponse = dateReponse;
        this.auteur = auteur;
        this.reclamationId = reclamationId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public LocalDateTime getDateReponse() { return dateReponse; }
    public void setDateReponse(LocalDateTime dateReponse) { this.dateReponse = dateReponse; }

    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }

    public int getReclamationId() { return reclamationId; }
    public void setReclamationId(int reclamationId) { this.reclamationId = reclamationId; }

    @Override
    public String toString() {
        return "[" + id + "] " + auteur + " : " + contenu;
    }
}