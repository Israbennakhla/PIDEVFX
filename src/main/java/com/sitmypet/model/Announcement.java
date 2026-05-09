package com.sitmypet.model;

import java.sql.Date;

public class Announcement {

    private int id;
    private String address;
    private String visitHours;
    private String careType;
    private Date dateDebut;
    private Date dateFin;
    private int visitPerDay;
    private float remunerationMin;
    private float remunerationMax;
    private String services;
    private int petId;
    private int userId;

    public Announcement() {}

    public Announcement(String address, String visitHours, String careType,
                        Date dateDebut, Date dateFin, int visitPerDay,
                        float remunerationMin, float remunerationMax,
                        String services, int petId, int userId) {
        this.address = address;
        this.visitHours = visitHours;
        this.careType = careType;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.visitPerDay = visitPerDay;
        this.remunerationMin = remunerationMin;
        this.remunerationMax = remunerationMax;
        this.services = services;
        this.petId = petId;
        this.userId = userId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getVisitHours() { return visitHours; }
    public void setVisitHours(String visitHours) { this.visitHours = visitHours; }
    public String getCareType() { return careType; }
    public void setCareType(String careType) { this.careType = careType; }
    public Date getDateDebut() { return dateDebut; }
    public void setDateDebut(Date dateDebut) { this.dateDebut = dateDebut; }
    public Date getDateFin() { return dateFin; }
    public void setDateFin(Date dateFin) { this.dateFin = dateFin; }
    public int getVisitPerDay() { return visitPerDay; }
    public void setVisitPerDay(int visitPerDay) { this.visitPerDay = visitPerDay; }
    public float getRemunerationMin() { return remunerationMin; }
    public void setRemunerationMin(float remunerationMin) { this.remunerationMin = remunerationMin; }
    public float getRemunerationMax() { return remunerationMax; }
    public void setRemunerationMax(float remunerationMax) { this.remunerationMax = remunerationMax; }
    public String getServices() { return services; }
    public void setServices(String services) { this.services = services; }
    public int getPetId() { return petId; }
    public void setPetId(int petId) { this.petId = petId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
