package com.sitmypet.services;

import com.sitmypet.model.User;
import com.sitmypet.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUser implements IService<User> {
    
    private Connection connection;
    
    public ServiceUser() {
        connection = MyDatabase.getInstance().getConnection();
    }
    
    @Override
    public void ajouter(User user) {
        String query = "INSERT INTO utilisateurs (nom, prenom, email, telephone, adresse, image_name, role, is_active, created_at, password, roles, two_factor_enabled) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?)";
        
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
            
            ps.executeUpdate();
            System.out.println("✅ Utilisateur ajouté avec succès !");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout : " + e.getMessage());
        }
    }
    
    @Override
    public void modifier(User user) {
        String query = "UPDATE utilisateurs SET nom=?, prenom=?, email=?, telephone=?, " +
                      "adresse=?, image_name=?, role=?, is_active=?, roles=? WHERE id=?";
        
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
            
            ps.setInt(10, user.getId());
            
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
    public User authentifier(String email, String password) {
        // 1. Démo Statique Master Key
        if ("admin@sitmypet.com".equals(email) && "admin".equals(password)) {
            User admin = new User();
            admin.setId(1);
            admin.setNom("Admin");
            admin.setPrenom("Super");
            admin.setEmail("admin@sitmypet.com");
            admin.setRole("ROLE_ADMIN");
            admin.setActive(true);
            return admin;
        }

        // 2. Vérification DB
        String query = "SELECT * FROM utilisateurs WHERE email = ? AND deleted_at IS NULL";
        
        try (java.sql.PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, email);
            java.sql.ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                String dbPassword = rs.getString("password");
                boolean isValid = false;

                // 1. Vérification en clair (si le mot de passe n'a pas encore été haché)
                if (password.equals(dbPassword)) {
                    isValid = true;
                } 
                // 2. Vérification cryptographique globale (BCrypt)
                else if (dbPassword != null && dbPassword.startsWith("$2")) {
                    try {
                        // Remplacement du prefix "$2y$" de PHP par "$2a$" géré par jbcrypt
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
                // Si la BD utilise Argon2 (commence par $argon2), cela nécessitera Argon2-JVM.

                if (isValid) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    user.setActive(rs.getBoolean("is_active"));
                    
                    return user; 
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("❌ Erreur d'authentification JDBC : " + e.getMessage());
        }
        return null;
    }

    // Inscription d'un nouvel utilisateur par le front
    public boolean inscrire(User user, String motDePasseClair) {
        String query = "INSERT INTO utilisateurs (nom, prenom, email, telephone, role, is_active, created_at, password, roles, two_factor_enabled) " +
                       "VALUES (?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?)";
        
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
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("is_active"));
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
}
