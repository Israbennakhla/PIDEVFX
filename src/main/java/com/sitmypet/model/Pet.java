package com.sitmypet.model;

import java.sql.Date;

public class Pet {

    private int id;
    private String name;
    private Date birthDate;
    private String typePet;
    private String breed;
    private float weight;
    private String description;
    private String gender;
    private boolean hasContagiousDisease;
    private boolean hasMedicalRecord;
    private boolean hasCriticalCondition;
    private boolean isVaccinated;
    private String imageName;
    private int ownerId;

    public Pet() {}

    public Pet(String name, Date birthDate, String typePet, String breed,
               float weight, String description, String gender,
               boolean hasContagiousDisease, boolean hasMedicalRecord,
               boolean hasCriticalCondition, boolean isVaccinated,
               String imageName, int ownerId) {
        this.name = name;
        this.birthDate = birthDate;
        this.typePet = typePet;
        this.breed = breed;
        this.weight = weight;
        this.description = description;
        this.gender = gender;
        this.hasContagiousDisease = hasContagiousDisease;
        this.hasMedicalRecord = hasMedicalRecord;
        this.hasCriticalCondition = hasCriticalCondition;
        this.isVaccinated = isVaccinated;
        this.imageName = imageName;
        this.ownerId = ownerId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Date getBirthDate() { return birthDate; }
    public void setBirthDate(Date birthDate) { this.birthDate = birthDate; }
    public String getTypePet() { return typePet; }
    public void setTypePet(String typePet) { this.typePet = typePet; }
    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }
    public float getWeight() { return weight; }
    public void setWeight(float weight) { this.weight = weight; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public boolean isHasContagiousDisease() { return hasContagiousDisease; }
    public void setHasContagiousDisease(boolean v) { this.hasContagiousDisease = v; }
    public boolean isHasMedicalRecord() { return hasMedicalRecord; }
    public void setHasMedicalRecord(boolean v) { this.hasMedicalRecord = v; }
    public boolean isHasCriticalCondition() { return hasCriticalCondition; }
    public void setHasCriticalCondition(boolean v) { this.hasCriticalCondition = v; }
    public boolean isVaccinated() { return isVaccinated; }
    public void setVaccinated(boolean v) { this.isVaccinated = v; }
    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }
    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }
}
