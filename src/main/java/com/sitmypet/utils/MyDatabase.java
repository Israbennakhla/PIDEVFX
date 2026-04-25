package com.sitmypet.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {
    private static MyDatabase instance;
    private Connection connection;
    
    // Paramètres de connexion
    private static final String URL = "jdbc:mysql://localhost:3306/sitmypet";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Vide pour XAMPP
    
    private MyDatabase() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connexion à la base de données réussie !");
            try {
                connection.createStatement().executeUpdate("ALTER TABLE utilisateurs ADD COLUMN certificat VARCHAR(255) DEFAULT NULL");
            } catch (Exception e) {}
            try {
                connection.createStatement().executeUpdate("ALTER TABLE utilisateurs ADD COLUMN failed_login_attempts INT DEFAULT 0");
            } catch (Exception e) {}
            try {
                connection.createStatement().executeUpdate("ALTER TABLE utilisateurs ADD COLUMN lockout_time DATETIME DEFAULT NULL");
            } catch (Exception e) {}
            System.out.println("✅ Structure de la base de données vérifiée/mise à jour.");
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion : " + e.getMessage());
        }
    }
    
    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }
    
    public Connection getConnection() {
        return connection;
    }
}
