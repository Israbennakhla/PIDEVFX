package com.sitmypet.model;

import java.sql.Date;

public class Postulation {
    private int    id;
    private int    announcementId;
    private int    gardienId;
    private Date   datePostulation;
    private String statut; // "EN_ATTENTE", "ACCEPTE", "REFUSE", "ANNULE"

    public Postulation() {}

    public Postulation(int announcementId, int gardienId, Date datePostulation, String statut) {
        this.announcementId  = announcementId;
        this.gardienId       = gardienId;
        this.datePostulation = datePostulation;
        this.statut          = statut;
    }

    public int    getId()               { return id; }
    public void   setId(int id)         { this.id = id; }

    public int    getAnnouncementId()              { return announcementId; }
    public void   setAnnouncementId(int v)         { this.announcementId = v; }

    public int    getGardienId()                   { return gardienId; }
    public void   setGardienId(int v)              { this.gardienId = v; }

    public Date   getDatePostulation()             { return datePostulation; }
    public void   setDatePostulation(Date v)       { this.datePostulation = v; }

    public String getStatut()                      { return statut; }
    public void   setStatut(String v)              { this.statut = v; }
}