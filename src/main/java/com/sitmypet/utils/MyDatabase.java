package com.sitmypet.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private static MyDataBase instance;

    private final String URL = "jdbc:mysql://127.0.0.1:3306/sitmypet";
    private final String USERNAME = "root";
    private final String PASSWORD = "";

    private Connection cnx;

    private MyDataBase() {
        try {
            cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connected ...");
            fixDatabaseSchema();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null)
            instance = new MyDataBase();
        return instance;
    }

    private void fixDatabaseSchema() {
        try {
            java.sql.Statement st = cnx.createStatement();
            
            // Ensure event table exists
            try { st.executeUpdate("CREATE TABLE IF NOT EXISTS `event` ("
                    + "`id` INT AUTO_INCREMENT PRIMARY KEY, "
                    + "`name` VARCHAR(255) NOT NULL, "
                    + "`date` DATE, "
                    + "`heure` VARCHAR(10), "
                    + "`addresse` TEXT, "
                    + "`description` TEXT"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"); } catch(Exception ignored) {}
            
            // Ensure event_participants table exists
            try { st.executeUpdate("CREATE TABLE IF NOT EXISTS `event_participants` ("
                    + "`id` INT AUTO_INCREMENT PRIMARY KEY, "
                    + "`event_id` INT NOT NULL, "
                    + "`user_id` INT NOT NULL, "
                    + "UNIQUE KEY `unique_enrollment` (`event_id`, `user_id`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"); } catch(Exception ignored) {}
            
            // Fix notification table
            try { st.executeUpdate("ALTER TABLE notification ADD COLUMN destinataire_id INT NOT NULL"); } catch(Exception ignored) {}
            try { st.executeUpdate("ALTER TABLE notification ADD COLUMN expediteur_id INT NOT NULL"); } catch(Exception ignored) {}
            try { st.executeUpdate("ALTER TABLE notification ADD COLUMN postulation_id INT NOT NULL"); } catch(Exception ignored) {}
            try { st.executeUpdate("ALTER TABLE notification ADD COLUMN message TEXT NOT NULL"); } catch(Exception ignored) {}
            try { st.executeUpdate("ALTER TABLE notification ADD COLUMN type VARCHAR(30) NOT NULL"); } catch(Exception ignored) {}
            try { st.executeUpdate("ALTER TABLE notification ADD COLUMN lu BOOLEAN NOT NULL DEFAULT FALSE"); } catch(Exception ignored) {}
            try { st.executeUpdate("ALTER TABLE notification ADD COLUMN date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP"); } catch(Exception ignored) {}
            
            // Fix postulation table
            try { st.executeUpdate("ALTER TABLE postulation ADD COLUMN date_postulation DATE NOT NULL"); } catch(Exception ignored) {}
            try { st.executeUpdate("ALTER TABLE postulation ADD COLUMN statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE'"); } catch(Exception ignored) {}
            try { st.executeUpdate("ALTER TABLE postulation ADD COLUMN announcement_id INT NOT NULL"); } catch(Exception ignored) {}
            try { st.executeUpdate("ALTER TABLE postulation ADD COLUMN gardien_id INT NOT NULL"); } catch(Exception ignored) {}
            
            // Fix message table
            try { st.executeUpdate("ALTER TABLE message ADD COLUMN expediteur_id INT NOT NULL"); } catch(Exception ignored) {}
            try { st.executeUpdate("ALTER TABLE message ADD COLUMN destinataire_id INT NOT NULL"); } catch(Exception ignored) {}
            try { st.executeUpdate("ALTER TABLE message ADD COLUMN postulation_id INT NOT NULL"); } catch(Exception ignored) {}
            try { st.executeUpdate("ALTER TABLE message ADD COLUMN contenu TEXT NOT NULL"); } catch(Exception ignored) {}
            try { st.executeUpdate("ALTER TABLE message ADD COLUMN date_envoi DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP"); } catch(Exception ignored) {}
            try { st.executeUpdate("ALTER TABLE message ADD COLUMN lu BOOLEAN NOT NULL DEFAULT FALSE"); } catch(Exception ignored) {}
            
            System.out.println("Schema verification completed.");
        } catch (SQLException e) {
            System.out.println("Schema check error: " + e.getMessage());
        }
    }

    public Connection getCnx() {
        return cnx;
    }

    public Connection getConnection() {
        return cnx;
    }
}
