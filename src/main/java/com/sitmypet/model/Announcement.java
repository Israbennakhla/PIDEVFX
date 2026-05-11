package com.sitmypet.model;

import java.sql.Date;

public class Announcement {

    private int    id;
    private String address;
    private String visitHours;       // ex: "08:00,12:00,18:00"
    private String careType;         // "CHEZ_MOI" ou "EN_CHENIL"
    private Date   dateDebut;
    private Date   dateFin;
    private int    visitPerDay;
    private float  remunerationMin;
    private float  remunerationMax;
    private String services;
    private int    petId;
    private int    userId;

    public Announcement() {}

    public Announcement(String address, String visitHours, String careType,
                        Date dateDebut, Date dateFin, int visitPerDay,
                        float remunerationMin, float remunerationMax,
                        String services, int petId, int userId) {
        this.address         = address;
        this.visitHours      = visitHours;
        this.careType        = careType;
        this.dateDebut       = dateDebut;
        this.dateFin         = dateFin;
        this.visitPerDay     = visitPerDay;
        this.remunerationMin = remunerationMin;
        this.remunerationMax = remunerationMax;
        this.services        = services;
        this.petId           = petId;
        this.userId          = userId;
    }

    public int    getId()                      { return id; }
    public void   setId(int id)                { this.id = id; }
    public String getAddress()                 { return address; }
    public void   setAddress(String v)         { this.address = v; }
    public String getVisitHours()              { return visitHours; }
    public void   setVisitHours(String v)      { this.visitHours = v; }
    public String getCareType()                { return careType; }
    public void   setCareType(String v)        { this.careType = v; }
    public Date   getDateDebut()               { return dateDebut; }
    public void   setDateDebut(Date v)         { this.dateDebut = v; }
    public Date   getDateFin()                 { return dateFin; }
    public void   setDateFin(Date v)           { this.dateFin = v; }
    public int    getVisitPerDay()             { return visitPerDay; }
    public void   setVisitPerDay(int v)        { this.visitPerDay = v; }
    public float  getRemunerationMin()         { return remunerationMin; }
    public void   setRemunerationMin(float v)  { this.remunerationMin = v; }
    public float  getRemunerationMax()         { return remunerationMax; }
    public void   setRemunerationMax(float v)  { this.remunerationMax = v; }
    public String getServices()                { return services; }
    public void   setServices(String v)        { this.services = v; }
    public int    getPetId()                   { return petId; }
    public void   setPetId(int v)             { this.petId = v; }
    public int    getUserId()                  { return userId; }
    public void   setUserId(int v)            { this.userId = v; }
}
