package model;

import java.sql.Date;

public class Evenement {
    private int id;
    private String name;
    private Date date;
    private String heure;
    private String addresse;
    private String description;

    public Evenement() {}

    public Evenement(int id, String name, Date date, String heure, String addresse, String description) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.heure = heure;
        this.addresse = addresse;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getHeure() { return heure; }
    public void setHeure(String heure) { this.heure = heure; }

    public String getAddresse() { return addresse; }
    public void setAddresse(String addresse) { this.addresse = addresse; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return name + " (" + date + " à " + heure + ") - " + addresse;
    }
}
