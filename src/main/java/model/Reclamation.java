package model;

import java.time.LocalDateTime;

public class Reclamation {
    private int id;
    private String sujet;
    private String description;
    private LocalDateTime dateReclamation;  // date_reclamation datetime
    private String statut;
    private String priorite;
    private String nomClient;               // nom_client
    private String emailClient;             // email_client
    private int userId;                     // user_id

    public Reclamation() {}

    public Reclamation(String sujet, String description, LocalDateTime dateReclamation,
                       String statut, String priorite, String nomClient,
                       String emailClient, int userId) {
        this.sujet = sujet;
        this.description = description;
        this.dateReclamation = dateReclamation;
        this.statut = statut;
        this.priorite = priorite;
        this.nomClient = nomClient;
        this.emailClient = emailClient;
        this.userId = userId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSujet() { return sujet; }
    public void setSujet(String sujet) { this.sujet = sujet; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateReclamation() { return dateReclamation; }
    public void setDateReclamation(LocalDateTime d) { this.dateReclamation = d; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }

    public String getEmailClient() { return emailClient; }
    public void setEmailClient(String emailClient) { this.emailClient = emailClient; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    @Override
    public String toString() {
        return "[" + id + "] " + sujet + " — " + priorite + " (" + statut + ")";
    }
}