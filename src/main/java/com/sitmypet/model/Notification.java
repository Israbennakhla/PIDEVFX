package com.sitmypet.model;

import java.sql.Timestamp;

public class Notification {
    private int       id;
    private int       destinataireId;
    private int       expediteurId;
    private int       postulationId;
    private String    message;
    private String    type;
    private boolean   lu;
    private Timestamp dateCreation;

    public Notification() {}

    public Notification(int destinataireId, int expediteurId, int postulationId,
                        String message, String type) {
        this.destinataireId = destinataireId;
        this.expediteurId   = expediteurId;
        this.postulationId  = postulationId;
        this.message        = message;
        this.type           = type;
        this.lu             = false;
    }

    public int       getId()               { return id; }
    public void      setId(int id)         { this.id = id; }
    public int       getDestinataireId()   { return destinataireId; }
    public void      setDestinataireId(int v) { this.destinataireId = v; }
    public int       getExpediteurId()     { return expediteurId; }
    public void      setExpediteurId(int v)   { this.expediteurId = v; }
    public int       getPostulationId()    { return postulationId; }
    public void      setPostulationId(int v)  { this.postulationId = v; }
    public String    getMessage()          { return message; }
    public void      setMessage(String v)  { this.message = v; }
    public String    getType()             { return type; }
    public void      setType(String v)     { this.type = v; }
    public boolean   isLu()               { return lu; }
    public void      setLu(boolean v)     { this.lu = v; }
    public Timestamp getDateCreation()     { return dateCreation; }
    public void      setDateCreation(Timestamp v) { this.dateCreation = v; }
}