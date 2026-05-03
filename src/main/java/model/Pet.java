package model;

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

    // =========================
    // Constructeur vide
    // =========================
    public Pet() {
    }

    // =========================
    // Constructeur COMPLET (UTILISÉ)
    // =========================
    public Pet(String name, Date birthDate, String typePet, String breed,
               float weight, String description, String gender,
               boolean hasContagiousDisease, boolean hasMedicalRecord,
               boolean hasCriticalCondition, boolean isVaccinated,
               String imageName) {

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
    }

    // =========================
    // GETTERS / SETTERS
    // =========================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getTypePet() {
        return typePet;
    }

    public void setTypePet(String typePet) {
        this.typePet = typePet;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public boolean isHasContagiousDisease() {
        return hasContagiousDisease;
    }

    public void setHasContagiousDisease(boolean hasContagiousDisease) {
        this.hasContagiousDisease = hasContagiousDisease;
    }

    public boolean isHasMedicalRecord() {
        return hasMedicalRecord;
    }

    public void setHasMedicalRecord(boolean hasMedicalRecord) {
        this.hasMedicalRecord = hasMedicalRecord;
    }

    public boolean isHasCriticalCondition() {
        return hasCriticalCondition;
    }

    public void setHasCriticalCondition(boolean hasCriticalCondition) {
        this.hasCriticalCondition = hasCriticalCondition;
    }

    public boolean isVaccinated() {
        return isVaccinated;
    }

    public void setVaccinated(boolean vaccinated) {
        isVaccinated = vaccinated;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    // =========================
    // toString
    // =========================
    @Override
    public String toString() {
        return "Pet{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", birthDate=" + birthDate +
                ", typePet='" + typePet + '\'' +
                ", breed='" + breed + '\'' +
                ", weight=" + weight +
                ", description='" + description + '\'' +
                ", gender='" + gender + '\'' +
                ", hasContagiousDisease=" + hasContagiousDisease +
                ", hasMedicalRecord=" + hasMedicalRecord +
                ", hasCriticalCondition=" + hasCriticalCondition +
                ", isVaccinated=" + isVaccinated +
                ", imageName='" + imageName + '\'' +
                '}';
    }
}