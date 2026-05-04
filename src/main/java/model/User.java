package model;

public class User {
    private int id;
    private String nom;
    private String prenom;
    private String email;

    public User() {}

    public User(int id, String nom, String prenom, String email) {
        this.id     = id;
        this.nom    = nom;
        this.prenom = prenom;
        this.email  = email;
    }

    public int    getId()     { return id; }
    public String getNom()    { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail()  { return email; }

    /** Displayed in the ComboBox dropdown. */
    @Override
    public String toString() {
        return prenom + " " + nom + "  —  " + email;
    }
}
