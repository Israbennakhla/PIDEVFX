package com.sitmypet.services;

import com.sitmypet.model.User;
import com.sitmypet.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.sitmypet.exceptions.AuthenticationException;

public class ServiceUser implements IService<User> {
    
    private Connection connection;
    
    public ServiceUser() {
        connection = MyDatabase.getInstance().getConnection();
    }
    
    @Override
    public void ajouter(User user) {
        String query = "INSERT INTO utilisateurs (nom, prenom, email, telephone, adresse, image_name, role, is_active, created_at, password, roles, two_factor_enabled, certificat) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getTelephone());
            ps.setString(5, user.getAdresse());
            ps.setString(6, user.getPhoto());
            
            // Formatage du rôle avec ROLE_ pour Symfony
            String rawRole = user.getRole() != null && !user.getRole().isEmpty() ? user.getRole() : "USER";
            String dbRole = rawRole.startsWith("ROLE_") ? rawRole : "ROLE_" + rawRole;
            
            ps.setString(7, dbRole);
            ps.setBoolean(8, user.isActive());
            
            // Valeurs par défaut pour les champs NOT NULL requis par la base de données Symfony/Doctrine
            ps.setString(9, "motdepassepardefaut"); 
            String jsonRole = "[\"" + dbRole + "\"]";
            ps.setString(10, jsonRole);
            ps.setBoolean(11, false);
            ps.setString(12, user.getCertificat());
            
            ps.executeUpdate();
            System.out.println("✅ Utilisateur ajouté avec succès !");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout : " + e.getMessage());
        }
    }
    
    @Override
    public void modifier(User user) {
        String query = "UPDATE utilisateurs SET nom=?, prenom=?, email=?, telephone=?, " +
                      "adresse=?, image_name=?, role=?, is_active=?, roles=?, certificat=? WHERE id=?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getTelephone());
            ps.setString(5, user.getAdresse());
            ps.setString(6, user.getPhoto());
            
            // Formatage du rôle
            String rawRole = user.getRole() != null && !user.getRole().isEmpty() ? user.getRole() : "USER";
            String dbRole = rawRole.startsWith("ROLE_") ? rawRole : "ROLE_" + rawRole;
            
            ps.setString(7, dbRole);
            ps.setBoolean(8, user.isActive());
            
            // Mettre à jour également le champ JSON roles
            String jsonRole = "[\"" + dbRole + "\"]";
            ps.setString(9, jsonRole);
            ps.setString(10, user.getCertificat());
            ps.setInt(11, user.getId());
            
            ps.executeUpdate();
            System.out.println("✅ Utilisateur modifié avec succès !");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification : " + e.getMessage());
        }
    }
    
    @Override
    public void supprimer(int id) {
        String query = "UPDATE utilisateurs SET deleted_at = NOW() WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Utilisateur supprimé avec succès (Soft Delete) !");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression : " + e.getMessage());
        }
    }
    
    @Override
    public List<User> afficher() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM utilisateurs WHERE deleted_at IS NULL ORDER BY created_at DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setTelephone(rs.getString("telephone"));
                user.setAdresse(rs.getString("adresse"));
                user.setPhoto(rs.getString("image_name"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("is_active"));
                user.setCertificat(rs.getString("certificat"));
                
                Timestamp timestamp = rs.getTimestamp("created_at");
                if (timestamp != null) {
                    user.setCreatedAt(timestamp.toLocalDateTime());
                }
                
                users.add(user);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'affichage : " + e.getMessage());
        }
        
        return users;
    }
    
    // Méthode de recherche avancée avec filtres et tri
    public List<User> rechercher(String searchTerm, String roleFilter, String statusFilter, String triFilter) {
        List<User> users = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT * FROM utilisateurs WHERE deleted_at IS NULL");
        
        if (searchTerm != null && !searchTerm.isEmpty()) {
            query.append(" AND (nom LIKE ? OR prenom LIKE ?)");
        }
        
        if (roleFilter != null && !roleFilter.isEmpty() && !roleFilter.equals("Tous")) {
            query.append(" AND role LIKE ?");
        }
        
        if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equals("Tous")) {
            if (statusFilter.equals("Actif")) {
                query.append(" AND is_active = 1");
            } else if (statusFilter.equals("Inactif")) {
                query.append(" AND is_active = 0");
            }
        }
        
        if (triFilter != null) {
            if (triFilter.equals("Nom (A-Z)")) {
                query.append(" ORDER BY nom ASC");
            } else if (triFilter.equals("Nom (Z-A)")) {
                query.append(" ORDER BY nom DESC");
            } else {
                query.append(" ORDER BY created_at DESC");
            }
        } else {
            query.append(" ORDER BY created_at DESC");
        }
        
        try (PreparedStatement ps = connection.prepareStatement(query.toString())) {
            int paramIndex = 1;
            
            if (searchTerm != null && !searchTerm.isEmpty()) {
                // Recherche uniquement sur les premiers caractères (commence par)
                String searchPattern = searchTerm + "%";
                ps.setString(paramIndex++, searchPattern);
                ps.setString(paramIndex++, searchPattern);
            }
            
            if (roleFilter != null && !roleFilter.isEmpty() && !roleFilter.equals("Tous")) {
                ps.setString(paramIndex++, "%" + roleFilter + "%");
            }
            
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setTelephone(rs.getString("telephone"));
                user.setAdresse(rs.getString("adresse"));
                user.setPhoto(rs.getString("image_name"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("is_active"));
                user.setCertificat(rs.getString("certificat"));
                
                Timestamp timestamp = rs.getTimestamp("created_at");
                if (timestamp != null) {
                    user.setCreatedAt(timestamp.toLocalDateTime());
                }
                
                users.add(user);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche : " + e.getMessage());
        }
        
        return users;
    }

    // Authentification de la page Login
    public User authentifier(String email, String password) throws AuthenticationException {
        String query = "SELECT * FROM utilisateurs WHERE email = ? AND deleted_at IS NULL";
        
        try (java.sql.PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, email);
            java.sql.ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                int userId = rs.getInt("id");
                
                // Vérification du verrouillage de compte
                Timestamp lockoutTime = null;
                int attempts = 0;
                try {
                    lockoutTime = rs.getTimestamp("lockout_time");
                    attempts = rs.getInt("failed_login_attempts");
                } catch (SQLException e) {
                    // Les colonnes peuvent ne pas exister sur de vieilles BD avant le patch
                }

                if (lockoutTime != null) {
                    long now = System.currentTimeMillis();
                    if (lockoutTime.getTime() > now) {
                        if (attempts >= 8) {
                            throw new AuthenticationException("⛔ Votre compte est bloqué définitivement pour raisons de sécurité. Veuillez contacter le support.", lockoutTime.getTime());
                        } else {
                            throw new AuthenticationException("⏳ Trop de tentatives échouées. Compte bloqué.", lockoutTime.getTime());
                        }
                    }
                }

                String dbPassword = rs.getString("password");
                boolean isValid = false;

                // 1. Vérification en clair (si le mot de passe n'a pas encore été haché)
                if (password.equals(dbPassword)) {
                    isValid = true;
                } 
                // 2. Vérification cryptographique globale (BCrypt)
                else if (dbPassword != null && dbPassword.startsWith("$2")) {
                    try {
                        String hashToCheck = dbPassword;
                        if (hashToCheck.startsWith("$2y$")) {
                            hashToCheck = "$2a$" + hashToCheck.substring(4);
                        }
                        if (org.mindrot.jbcrypt.BCrypt.checkpw(password, hashToCheck)) {
                            isValid = true;
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Erreur de vérification BCrypt : " + e.getMessage());
                    }
                }

                if (isValid) {
                    // Succès : on réinitialise les tentatives
                    try (PreparedStatement updatePs = connection.prepareStatement("UPDATE utilisateurs SET failed_login_attempts = 0, lockout_time = NULL WHERE id = ?")) {
                        updatePs.setInt(1, userId);
                        updatePs.executeUpdate();
                    } catch (Exception e) {}

                    User user = new User();
                    user.setId(userId);
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    user.setEmail(rs.getString("email"));
                    user.setTelephone(rs.getString("telephone"));
                    user.setAdresse(rs.getString("adresse"));
                    user.setPhoto(rs.getString("image_name"));
                    user.setRole(rs.getString("role"));
                    user.setActive(rs.getBoolean("is_active"));
                    user.setCertificat(rs.getString("certificat"));
                    
                    java.sql.Timestamp timestamp = rs.getTimestamp("created_at");
                    if (timestamp != null) {
                        user.setCreatedAt(timestamp.toLocalDateTime());
                    }
                    
                    return user; 
                } else {
                    // Échec du mot de passe : incrémenter les tentatives et vérifier la politique de blocage
                    attempts++;
                    Timestamp newLockout = null;
                    
                    if (attempts >= 8) {
                        newLockout = new Timestamp(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365 * 10); // 10 ans = infini
                        new EmailService().envoyerAlerteBlocage(email);
                    } else if (attempts == 6) {
                        newLockout = new Timestamp(System.currentTimeMillis() + 15 * 60000);
                    } else if (attempts == 3) {
                        newLockout = new Timestamp(System.currentTimeMillis() + 5 * 60000);
                    }
                    
                    String updateQuery = (attempts >= 8) ? 
                        "UPDATE utilisateurs SET failed_login_attempts = ?, lockout_time = ?, is_active = 0 WHERE id = ?" :
                        "UPDATE utilisateurs SET failed_login_attempts = ?, lockout_time = ? WHERE id = ?";
                        
                    try (PreparedStatement updatePs = connection.prepareStatement(updateQuery)) {
                        updatePs.setInt(1, attempts);
                        updatePs.setTimestamp(2, newLockout);
                        updatePs.setInt(3, userId);
                        updatePs.executeUpdate();
                    } catch (Exception e) {}

                    if (attempts >= 8) {
                        throw new AuthenticationException("⛔ Mot de passe incorrect (8e tentative). Votre compte a été bloqué définitivement pour votre sécurité. Un email vous a été envoyé.", newLockout.getTime());
                    } else if (attempts == 6) {
                        throw new AuthenticationException("⛔ Mot de passe incorrect (6e tentative). Votre compte est bloqué pendant 15 minutes.", newLockout.getTime());
                    } else if (attempts == 3) {
                        throw new AuthenticationException("⛔ Mot de passe incorrect (3e tentative). Votre compte est bloqué pendant 5 minutes.", newLockout.getTime());
                    } else {
                        throw new AuthenticationException("Email non reconnu ou mot de passe invalide. (Tentative " + attempts + ")");
                    }
                }
            } else {
                throw new AuthenticationException("Email non reconnu ou mot de passe invalide.");
            }
        } catch (java.sql.SQLException e) {
            System.err.println("❌ Erreur d'authentification JDBC : " + e.getMessage());
            throw new AuthenticationException("Erreur de connexion à la base de données.");
        }
    }

    // Trouver un utilisateur par email (utile pour la réinitialisation de mot de passe)
    public User trouverParEmail(String email) {
        String query = "SELECT * FROM utilisateurs WHERE email = ? AND deleted_at IS NULL";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setTelephone(rs.getString("telephone"));
                user.setAdresse(rs.getString("adresse"));
                user.setPhoto(rs.getString("image_name"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("is_active"));
                user.setCertificat(rs.getString("certificat"));
                java.sql.Timestamp timestamp = rs.getTimestamp("created_at");
                if (timestamp != null) {
                    user.setCreatedAt(timestamp.toLocalDateTime());
                }
                return user;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur JDBC lors de la recherche par email : " + e.getMessage());
        }
        return null;
    }

    // Changer le mot de passe d'un utilisateur (haché avec BCrypt)
    public boolean changerMotDePasse(String email, String nouveauMotDePasseClair) {
        String query = "UPDATE utilisateurs SET password = ? WHERE email = ? AND deleted_at IS NULL";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(nouveauMotDePasseClair, org.mindrot.jbcrypt.BCrypt.gensalt());
            // Remplacement de $2a$ (jbcrypt) par $2y$ pour compatibilité totale Symfony
            hashedPassword = hashedPassword.replaceFirst("^\\$2a\\$", "\\$2y\\$");
            
            pst.setString(1, hashedPassword);
            pst.setString(2, email);
            
            int rowsAffected = pst.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur JDBC lors du changement de mot de passe : " + e.getMessage());
        }
        return false;
    }

    // Inscription d'un nouvel utilisateur par le front
    public boolean inscrire(User user, String motDePasseClair) {
        String query = "INSERT INTO utilisateurs (nom, prenom, email, telephone, role, is_active, created_at, password, roles, two_factor_enabled, certificat) " +
                       "VALUES (?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getTelephone() != null ? user.getTelephone() : "");
            
            // Formatage du rôle
            String rawRole = user.getRole() != null && !user.getRole().isEmpty() ? user.getRole() : "USER";
            String dbRole = rawRole.startsWith("ROLE_") ? rawRole : "ROLE_" + rawRole;
            
            ps.setString(5, dbRole);
            ps.setBoolean(6, user.isActive());
            
            // Hachage cryptographique
            String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(motDePasseClair, org.mindrot.jbcrypt.BCrypt.gensalt());
            // Remplacement de $2a$ (jbcrypt) par $2y$ pour compatibilité totale Symfony
            hashedPassword = hashedPassword.replaceFirst("^\\$2a\\$", "\\$2y\\$");
            
            ps.setString(7, hashedPassword);
            
            String jsonRole = "[\"" + dbRole + "\"]";
            ps.setString(8, jsonRole);
            ps.setBoolean(9, false);
            ps.setString(10, user.getCertificat());
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Inscription réussie !");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'inscription : " + e.getMessage());
        }
        return false;
    }

    // Authentification ou Inscription via Google
    public User authentifierOuInscrireGoogle(String email, String nom, String prenom, String photoUrl) {
        // 1. Vérifier si l'utilisateur existe déjà
        String checkQuery = "SELECT * FROM utilisateurs WHERE email = ? AND deleted_at IS NULL";
        try (PreparedStatement pst = connection.prepareStatement(checkQuery)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                // L'utilisateur existe, on le connecte
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setTelephone(rs.getString("telephone"));
                user.setAdresse(rs.getString("adresse"));
                user.setPhoto(rs.getString("image_name"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("is_active"));
                user.setCertificat(rs.getString("certificat"));
                java.sql.Timestamp timestamp = rs.getTimestamp("created_at");
                if (timestamp != null) {
                    user.setCreatedAt(timestamp.toLocalDateTime());
                }
                return user;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur JDBC lors de la vérification Google : " + e.getMessage());
        }

        // 2. Si l'utilisateur n'existe pas, on le crée (rôle par défaut PROPRIETAIRE)
        User newUser = new User(nom, prenom, email, "", "", photoUrl, "PROPRIETAIRE", true);
        
        // Générer un mot de passe très complexe et aléatoire pour ce compte Google
        String randomPassword = java.util.UUID.randomUUID().toString() + java.util.UUID.randomUUID().toString();
        
        boolean isRegistered = inscrire(newUser, randomPassword);
        
        if (isRegistered) {
            // On le récupère pour avoir son ID généré
            try (PreparedStatement pst = connection.prepareStatement(checkQuery)) {
                pst.setString(1, email);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    newUser.setId(rs.getInt("id"));
                    return newUser;
                }
            } catch (SQLException e) {
                System.err.println("❌ Erreur JDBC post-inscription Google : " + e.getMessage());
            }
        }
        
        return null;
    }

    // Vérifier si un compte est actuellement bloqué
    public boolean estBloque(int userId) {
        String query = "SELECT lockout_time FROM utilisateurs WHERE id = ? AND deleted_at IS NULL";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Timestamp lockoutTime = rs.getTimestamp("lockout_time");
                if (lockoutTime != null && lockoutTime.getTime() > System.currentTimeMillis()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur JDBC lors de la vérification du blocage : " + e.getMessage());
        }
        return false;
    }

    // Débloquer manuellement un compte (utilisé par l'admin)
    public void debloquerCompte(int userId) {
        String query = "UPDATE utilisateurs SET failed_login_attempts = 0, lockout_time = NULL, is_active = 1 WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, userId);
            pst.executeUpdate();
            System.out.println("✅ Compte utilisateur ID " + userId + " débloqué par l'admin.");
        } catch (SQLException e) {
            System.err.println("❌ Erreur JDBC lors du déblocage du compte : " + e.getMessage());
        }
    }

    /** Affichage messagerie / listes (PIDEVFX-nourb). */
    public String getDisplayNameById(int id) {
        String query = "SELECT prenom, nom FROM utilisateurs WHERE id = ? AND deleted_at IS NULL";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String p = rs.getString("prenom");
                String n = rs.getString("nom");
                if (p != null && n != null) return p + " " + n;
                if (p != null) return p;
                if (n != null) return n;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getDisplayNameById : " + e.getMessage());
        }
        return "Profil utilisateur";
    }
}
