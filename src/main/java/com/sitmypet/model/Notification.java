package com.sitmypet.model;

import java.sql.Timestamp;

public class Notification {
    private int id;
    private int destinataireId;
    private int expediteurId;
    private int postulationId;
    private String message;
    private String type;
    private boolean lu;
    private Timestamp dateCreation;

    public Notification() {}

    public Notification(int destinataireId, int expediteurId, int postulationId,
                        String message, String type) {
        this.destinataireId = destinataireId;
        this.expediteurId = expediteurId;
        this.postulationId = postulationId;
        this.message = message;
        this.type = type;
        this.lu = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getDestinataireId() { return destinataireId; }
    public void setDestinataireId(int destinataireId) { this.destinataireId = destinataireId; }
    public int getExpediteurId() { return expediteurId; }
    public void setExpediteurId(int expediteurId) { this.expediteurId = expediteurId; }
    public int getPostulationId() { return postulationId; }
    public void setPostulationId(int postulationId) { this.postulationId = postulationId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isLu() { return lu; }
    public void setLu(boolean lu) { this.lu = lu; }
    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }
}
