package com.sitmypet.model;

import java.sql.Timestamp;

public class Message {
    private int id;
    private int expediteurId;
    private int destinataireId;
    private int postulationId;
    private String contenu;
    private Timestamp dateEnvoi;
    private boolean lu;

    public Message() {}

    public Message(int expediteurId, int destinataireId, int postulationId, String contenu) {
        this.expediteurId = expediteurId;
        this.destinataireId = destinataireId;
        this.postulationId = postulationId;
        this.contenu = contenu;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getExpediteurId() { return expediteurId; }
    public void setExpediteurId(int expediteurId) { this.expediteurId = expediteurId; }
    public int getDestinataireId() { return destinataireId; }
    public void setDestinataireId(int destinataireId) { this.destinataireId = destinataireId; }
    public int getPostulationId() { return postulationId; }
    public void setPostulationId(int postulationId) { this.postulationId = postulationId; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public Timestamp getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(Timestamp dateEnvoi) { this.dateEnvoi = dateEnvoi; }
    public boolean isLu() { return lu; }
    public void setLu(boolean lu) { this.lu = lu; }
}
