package com.sitmypet.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MyDatabase {

    /** Compte admin par défaut (créé une seule fois si absent en base). */
    public static final String DEFAULT_ADMIN_EMAIL    = "admin@sitmypet.com";
    public static final String DEFAULT_ADMIN_PASSWORD = "AdminSitMyPet2026!";

    private static MyDatabase instance;
    private Connection connection;
    
    // Paramètres de connexion
    private static final String URL = "jdbc:mysql://localhost:3306/sitmypet2";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Vide pour XAMPP
    
    private MyDatabase() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connexion à la base de données réussie !");
            ensureUtilisateursTable(connection);
            try {
                connection.createStatement().executeUpdate("ALTER TABLE utilisateurs ADD COLUMN certificat VARCHAR(255) DEFAULT NULL");
            } catch (Exception e) {}
            try {
                connection.createStatement().executeUpdate("ALTER TABLE utilisateurs ADD COLUMN failed_login_attempts INT DEFAULT 0");
            } catch (Exception e) {}
            try {
                connection.createStatement().executeUpdate("ALTER TABLE utilisateurs ADD COLUMN lockout_time DATETIME DEFAULT NULL");
            } catch (Exception e) {}
            ensureSitmypetModuleTables(connection);
            ensureDefaultAdminUser(connection);
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
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("Tentative de reconnexion à la base de données...");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur de reconnexion : " + e.getMessage());
        }
        return connection;
    }

    /**
     * Crée {@code utilisateurs} si elle n'existe pas (bases vides où seules les autres tables seraient créées).
     */
    private static void ensureUtilisateursTable(Connection cnx) {
        if (cnx == null) return;
        String ddl =
                "CREATE TABLE IF NOT EXISTS utilisateurs ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "nom VARCHAR(255) DEFAULT NULL,"
                + "prenom VARCHAR(255) DEFAULT NULL,"
                + "email VARCHAR(180) NOT NULL,"
                + "telephone VARCHAR(80) DEFAULT NULL,"
                + "adresse VARCHAR(512) DEFAULT NULL,"
                + "image_name VARCHAR(512) DEFAULT NULL,"
                + "role VARCHAR(80) NOT NULL DEFAULT 'ROLE_USER',"
                + "is_active TINYINT(1) NOT NULL DEFAULT 1,"
                + "created_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP,"
                + "password VARCHAR(255) NOT NULL,"
                + "roles LONGTEXT,"
                + "two_factor_enabled TINYINT(1) NOT NULL DEFAULT 0,"
                + "certificat VARCHAR(255) DEFAULT NULL,"
                + "deleted_at DATETIME DEFAULT NULL,"
                + "failed_login_attempts INT NOT NULL DEFAULT 0,"
                + "lockout_time DATETIME DEFAULT NULL,"
                + "UNIQUE KEY uq_utilisateurs_email (email),"
                + "KEY idx_utilisateurs_deleted_at (deleted_at)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        try (Statement st = cnx.createStatement()) {
            st.executeUpdate(ddl);
        } catch (SQLException e) {
            System.err.println("⚠️ Table utilisateurs : " + e.getMessage());
        }
    }

    /** Tables migrées depuis PIDEVFX-nourb (animaux, annonces, postulations, messagerie). */
    private static void ensureSitmypetModuleTables(Connection cnx) {
        String[] ddl = {
            "CREATE TABLE IF NOT EXISTS pet ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "name VARCHAR(255) NOT NULL,"
                + "birth_date DATE NULL,"
                + "type_pet VARCHAR(50) NULL,"
                + "breed VARCHAR(255) NULL,"
                + "weight FLOAT NULL,"
                + "description TEXT NULL,"
                + "gender VARCHAR(20) NULL,"
                + "has_contagious_disease TINYINT(1) NOT NULL DEFAULT 0,"
                + "has_medical_record TINYINT(1) NOT NULL DEFAULT 0,"
                + "has_critical_condition TINYINT(1) NOT NULL DEFAULT 0,"
                + "is_vaccinated TINYINT(1) NOT NULL DEFAULT 0,"
                + "image_name VARCHAR(512) NULL,"
                + "owner_id INT NOT NULL,"
                + "KEY idx_pet_owner (owner_id),"
                + "CONSTRAINT fk_pet_owner FOREIGN KEY (owner_id) REFERENCES utilisateurs(id) ON DELETE CASCADE"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

            "CREATE TABLE IF NOT EXISTS announcement ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "address TEXT NULL,"
                + "visit_hours JSON NULL,"
                + "care_type VARCHAR(50) NULL,"
                + "date_debut DATE NULL,"
                + "date_fin DATE NULL,"
                + "visit_per_day INT NULL,"
                + "renumeration_min FLOAT NULL,"
                + "renumeration_max FLOAT NULL,"
                + "services TEXT NULL,"
                + "pet_id INT NOT NULL,"
                + "user_id INT NOT NULL,"
                + "KEY idx_annonce_user (user_id),"
                + "KEY idx_annonce_pet (pet_id),"
                + "CONSTRAINT fk_announcement_pet FOREIGN KEY (pet_id) REFERENCES pet(id) ON DELETE CASCADE,"
                + "CONSTRAINT fk_announcement_user FOREIGN KEY (user_id) REFERENCES utilisateurs(id) ON DELETE CASCADE"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

            "CREATE TABLE IF NOT EXISTS postulation ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "announcement_id INT NOT NULL,"
                + "gardien_id INT NOT NULL,"
                + "date_postulation DATE NOT NULL,"
                + "statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',"
                + "KEY idx_post_ann (announcement_id),"
                + "KEY idx_post_gardien (gardien_id),"
                + "CONSTRAINT fk_post_ann FOREIGN KEY (announcement_id) REFERENCES announcement(id) ON DELETE CASCADE,"
                + "CONSTRAINT fk_post_gardien FOREIGN KEY (gardien_id) REFERENCES utilisateurs(id) ON DELETE CASCADE"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

            "CREATE TABLE IF NOT EXISTS message ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "expediteur_id INT NOT NULL,"
                + "destinataire_id INT NOT NULL,"
                + "postulation_id INT NOT NULL,"
                + "contenu TEXT NOT NULL,"
                + "date_envoi DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "lu TINYINT(1) NOT NULL DEFAULT 0,"
                + "KEY idx_msg_post (postulation_id),"
                + "CONSTRAINT fk_msg_post FOREIGN KEY (postulation_id) REFERENCES postulation(id) ON DELETE CASCADE"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

            "CREATE TABLE IF NOT EXISTS notification ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "destinataire_id INT NOT NULL,"
                + "expediteur_id INT NOT NULL,"
                + "postulation_id INT NOT NULL,"
                + "message TEXT NOT NULL,"
                + "type VARCHAR(30) NOT NULL,"
                + "lu TINYINT(1) NOT NULL DEFAULT 0,"
                + "date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "KEY idx_notif_dest (destinataire_id)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

            "CREATE TABLE IF NOT EXISTS reclamation ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "sujet VARCHAR(255) NOT NULL,"
                + "description TEXT NOT NULL,"
                + "date_reclamation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "statut VARCHAR(40) NOT NULL DEFAULT 'en_attente',"
                + "priorite VARCHAR(20) NOT NULL DEFAULT 'basse',"
                + "nom_client VARCHAR(150) NOT NULL,"
                + "email_client VARCHAR(180) NOT NULL,"
                + "user_id INT NOT NULL,"
                + "photo_url VARCHAR(767) DEFAULT NULL,"
                + "KEY idx_reclamation_user (user_id),"
                + "KEY idx_reclamation_statut (statut),"
                + "CONSTRAINT fk_reclamation_user FOREIGN KEY (user_id) REFERENCES utilisateurs(id) ON DELETE CASCADE"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

            "CREATE TABLE IF NOT EXISTS reponse ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "contenu TEXT NOT NULL,"
                + "date_reponse DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "auteur VARCHAR(180) NOT NULL,"
                + "reclamation_id INT NOT NULL,"
                + "KEY idx_reponse_reclamation (reclamation_id),"
                + "CONSTRAINT fk_reponse_reclamation FOREIGN KEY (reclamation_id) REFERENCES reclamation(id) ON DELETE CASCADE"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
        };
        try (Statement st = cnx.createStatement()) {
            for (String sql : ddl) {
                st.executeUpdate(sql);
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Schéma module SitMyPet : " + e.getMessage());
        }
    }

    /**
     * Insère un administrateur par défaut si aucun utilisateur avec {@link #DEFAULT_ADMIN_EMAIL} n'existe.
     * La détection se fait uniquement par e-mail (sans filtre {@code deleted_at}) pour éviter un échec silencieux
     * lorsque cette colonne n'existe pas encore. L'INSERT suit le même schéma que {@code ServiceUser.ajouter} avec mot de passe BCrypt.
     */
    private static void ensureDefaultAdminUser(Connection cnx) {
        if (cnx == null) return;
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT id FROM utilisateurs WHERE email = ? LIMIT 1")) {
            ps.setString(1, DEFAULT_ADMIN_EMAIL);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Admin par défaut : lecture utilisateurs : " + e.getMessage()
                    + " (SQLState=" + e.getSQLState() + ", err=" + e.getErrorCode() + ")");
            return;
        }

        String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(
                DEFAULT_ADMIN_PASSWORD, org.mindrot.jbcrypt.BCrypt.gensalt());
        hashedPassword = hashedPassword.replaceFirst("^\\$2a\\$", "\\$2y\\$");

        String ins = "INSERT INTO utilisateurs (nom, prenom, email, telephone, adresse, image_name, role, "
                + "is_active, created_at, password, roles, two_factor_enabled, certificat) "
                + "VALUES (?,?,?,?,?,?,?,?,NOW(),?,?,?,?)";

        try (PreparedStatement pst = cnx.prepareStatement(ins)) {
            pst.setString(1, "Admin");
            pst.setString(2, "SitMyPet");
            pst.setString(3, DEFAULT_ADMIN_EMAIL);
            pst.setString(4, "");
            pst.setString(5, "");
            pst.setString(6, "");
            pst.setString(7, "ROLE_ADMIN");
            pst.setBoolean(8, true);
            pst.setString(9, hashedPassword);
            pst.setString(10, "[\"ROLE_ADMIN\"]");
            pst.setBoolean(11, false);
            pst.setNull(12, java.sql.Types.VARCHAR);
            pst.executeUpdate();
            System.out.println("✅ Compte administrateur par défaut créé : " + DEFAULT_ADMIN_EMAIL);
        } catch (SQLException e) {
            System.err.println("⚠️ Impossible de créer l'admin par défaut : " + e.getMessage()
                    + " (SQLState=" + e.getSQLState() + ", err=" + e.getErrorCode() + ")");
        }
    }
}
