package com.sitmypet.services;

import com.sitmypet.model.Reclamation;
import com.sitmypet.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ServiceReclamation {

    private final Connection cnx;

    private static final String ORDER_SQL =
            "ORDER BY CASE statut "
                    + "WHEN 'en_attente' THEN 0 WHEN 'EN_ATTENTE' THEN 0 "
                    + "WHEN 'en_cours' THEN 1 WHEN 'EN_COURS' THEN 1 "
                    + "ELSE 2 END, date_reclamation DESC";

    public ServiceReclamation() {
        this.cnx = MyDatabase.getInstance().getConnection();
    }

    public String valider(Reclamation r) {
        if (r.getSujet() == null || r.getSujet().trim().isEmpty())
            return "Le sujet est obligatoire.";
        if (r.getSujet().trim().length() < 5)
            return "Le sujet doit contenir au moins 5 caractères.";
        if (r.getDescription() == null || r.getDescription().trim().length() < 10)
            return "La description doit contenir au moins 10 caractères.";
        if (r.getNomClient() == null || r.getNomClient().trim().isEmpty())
            return "Le nom du client est obligatoire.";
        if (r.getEmailClient() == null || !r.getEmailClient().contains("@"))
            return "Email invalide.";
        if (r.getPriorite() == null
                || !List.of("basse", "moyenne", "haute").contains(r.getPriorite()))
            return "Priorité invalide (basse / moyenne / haute).";
        return "OK";
    }

    /** Propriétaires et gardiens peuvent déposer une réclamation. */
    public boolean utilisateurPeutDeposerReclamation(int userId) {
        if (userId <= 0) return false;
        String sql = "SELECT role FROM utilisateurs WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return false;
            String role = rs.getString("role");
            if (role == null) return false;
            String u = role.toUpperCase();
            return u.contains("GARDIEN")
                    || u.contains("PROPRIETAIRE")
                    || u.contains("PROPRIÉTAIRE");
        } catch (SQLException e) {
            System.err.println("❌ utilisateurPeutDeposerReclamation : " + e.getMessage());
            return false;
        }
    }

    public void add(Reclamation r, int deposantUserId) {
        if (!utilisateurPeutDeposerReclamation(deposantUserId))
            throw new IllegalArgumentException(
                    "Seuls les comptes propriétaire ou gardien peuvent déposer une réclamation.");
        if (deposantUserId != r.getUserId())
            throw new IllegalArgumentException("Identifiant utilisateur incohérent.");

        String validation = valider(r);
        if (!validation.equals("OK"))
            throw new IllegalArgumentException(validation);

        try {
            PreparedStatement check = cnx.prepareStatement(
                    "SELECT COUNT(*) FROM reclamation WHERE sujet = ? AND email_client = ?");
            check.setString(1, r.getSujet());
            check.setString(2, r.getEmailClient());
            ResultSet rs = check.executeQuery();
            if (rs.next() && rs.getInt(1) > 0)
                throw new IllegalArgumentException("Une réclamation identique existe déjà pour ce courriel.");

            String insert = "INSERT INTO reclamation "
                    + "(sujet, description, date_reclamation, statut, priorite, nom_client, email_client, user_id, photo_url) "
                    + "VALUES (?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = cnx.prepareStatement(insert)) {
                ps.setString(1, r.getSujet());
                ps.setString(2, r.getDescription());
                ps.setTimestamp(3, r.getDateReclamation() != null ? Timestamp.valueOf(r.getDateReclamation()) : new Timestamp(System.currentTimeMillis()));
                ps.setString(4, normalizeStatut(r.getStatut() != null ? r.getStatut() : "en_attente"));
                ps.setString(5, r.getPriorite());
                ps.setString(6, r.getNomClient());
                ps.setString(7, r.getEmailClient());
                ps.setInt(8, r.getUserId());
                ps.setString(9, r.getPhotoUrl());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur ajout réclamation : " + e.getMessage(), e);
        }
    }

    /** Normalisation statut colonne BD (snake_case). */
    private static String normalizeStatut(String s) {
        if (s == null) return "en_attente";
        return switch (s.trim().toLowerCase()) {
            case "resolue" -> "resolue";
            case "en_cours" -> "en_cours";
            default -> "en_attente";
        };
    }

    public List<Reclamation> getAll() {
        List<Reclamation> list = new ArrayList<>();
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM reclamation " + ORDER_SQL)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Erreur getAll réclamation : " + e.getMessage());
        }
        return list;
    }

    public Reclamation getById(int id) {
        try (PreparedStatement ps = cnx.prepareStatement("SELECT * FROM reclamation WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("Erreur getById réclamation : " + e.getMessage());
        }
        return null;
    }

    public void delete(Reclamation r) {
        try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM reclamation WHERE id = ?")) {
            ps.setInt(1, r.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur delete réclamation : " + e.getMessage());
        }
    }

    /** Liste des réclamations d’un utilisateur (propriétaire / gardien) avec filtres. */
    public List<Reclamation> filterForUser(int userId, String nomPart, String statut, String priorite) {
        StringBuilder sb = new StringBuilder("SELECT * FROM reclamation WHERE user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);
        if (nomPart != null && !nomPart.isBlank()) {
            sb.append(" AND (nom_client LIKE ? OR sujet LIKE ?)");
            String like = "%" + nomPart.trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (statut != null && !statut.isBlank() && !"Tous".equals(statut)) {
            sb.append(" AND LOWER(TRIM(statut)) = ?");
            params.add(statut.trim().toLowerCase());
        }
        if (priorite != null && !priorite.isBlank() && !"Tous".equals(priorite)) {
            sb.append(" AND priorite = ?");
            params.add(priorite);
        }
        sb.append(" ").append(ORDER_SQL);
        List<Reclamation> out = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sb.toString())) {
            int i = 1;
            for (Object o : params) ps.setObject(i++, o);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) out.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Erreur filterForUser réclamation : " + e.getMessage());
        }
        return out;
    }

    /** Compte par statut pour un seul déposant. */
    public Map<String, Integer> countByStatutForUser(int userId) {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("en_attente", 0);
        stats.put("en_cours", 0);
        stats.put("resolue", 0);
        String sql = "SELECT LOWER(TRIM(statut)) AS s, COUNT(*) AS total FROM reclamation WHERE user_id = ? GROUP BY LOWER(TRIM(statut))";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String statut = rs.getString("s");
                int count = rs.getInt("total");
                if ("en_attente".equals(statut) || "en_cours".equals(statut) || "resolue".equals(statut))
                    stats.put(statut, count);
            }
        } catch (SQLException e) {
            System.err.println("Erreur countByStatutForUser : " + e.getMessage());
        }
        return stats;
    }

    /**
     * Mise à jour par le déposant : pas de changement de statut ni photo (gérés ailleurs).
     * Bloquée si une réponse admin existe.
     */
    public void updateByOwner(Reclamation r, int deposantUserId) {
        if (!utilisateurPeutDeposerReclamation(deposantUserId))
            throw new IllegalArgumentException("Compte non autorisé.");

        ServiceReponse reponses = new ServiceReponse();
        if (reponses.existsForReclamation(r.getId()))
            throw new IllegalArgumentException("Réclamation clôturée — modification impossible.");

        Reclamation db = getById(r.getId());
        if (db == null) throw new IllegalArgumentException("Réclamation introuvable.");
        if (db.getUserId() != deposantUserId)
            throw new IllegalArgumentException("Réclamation invalide.");

        r.setStatut(db.getStatut());
        r.setDateReclamation(db.getDateReclamation());
        r.setUserId(db.getUserId());
        r.setPhotoUrl(db.getPhotoUrl());

        String validation = valider(r);
        if (!validation.equals("OK"))
            throw new IllegalArgumentException(validation);

        String upt = "UPDATE reclamation SET sujet=?, description=?, priorite=?, nom_client=?, email_client=? WHERE id=? AND user_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(upt)) {
            ps.setString(1, r.getSujet().trim());
            ps.setString(2, r.getDescription().trim());
            ps.setString(3, r.getPriorite());
            ps.setString(4, r.getNomClient().trim());
            ps.setString(5, r.getEmailClient().trim());
            ps.setInt(6, r.getId());
            ps.setInt(7, deposantUserId);
            if (ps.executeUpdate() == 0)
                throw new IllegalArgumentException("Mise à jour refusée.");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur mise à jour réclamation : " + e.getMessage(), e);
        }
    }

    /** Suppression par le propriétaire de la ligne ; interdit si une réponse existe. */
    public void deleteForUser(int reclamationId, int userId) {
        ServiceReponse reponses = new ServiceReponse();
        if (reponses.existsForReclamation(reclamationId))
            throw new IllegalArgumentException("Impossible de supprimer : une réponse a été enregistrée.");

        Reclamation db = getById(reclamationId);
        if (db == null) throw new IllegalArgumentException("Réclamation introuvable.");
        if (db.getUserId() != userId)
            throw new IllegalArgumentException("Suppression refusée.");

        delete(db);
    }

    public List<Reclamation> filter(String nomPart, String statut, String priorite) {
        StringBuilder sb = new StringBuilder("SELECT * FROM reclamation WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (nomPart != null && !nomPart.isBlank()) {
            sb.append(" AND nom_client LIKE ?");
            params.add("%" + nomPart.trim() + "%");
        }
        if (statut != null && !statut.isBlank() && !"Tous".equals(statut)) {
            sb.append(" AND LOWER(TRIM(statut)) = ?");
            params.add(statut.trim().toLowerCase());
        }
        if (priorite != null && !priorite.isBlank() && !"Tous".equals(priorite)) {
            sb.append(" AND priorite = ?");
            params.add(priorite);
        }
        sb.append(" ").append(ORDER_SQL);
        List<Reclamation> out = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sb.toString())) {
            int i = 1;
            for (Object o : params) ps.setObject(i++, o);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) out.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Erreur filter réclamation : " + e.getMessage());
        }
        return out;
    }

    public Map<String, Integer> countByStatut() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("en_attente", 0);
        stats.put("en_cours", 0);
        stats.put("resolue", 0);
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery("SELECT LOWER(TRIM(statut)) AS s, COUNT(*) AS total FROM reclamation GROUP BY LOWER(TRIM(statut))")) {
            while (rs.next()) {
                String statut = rs.getString("s");
                int count = rs.getInt("total");
                if ("en_attente".equals(statut) || "en_cours".equals(statut) || "resolue".equals(statut))
                    stats.put(statut, count);
            }
        } catch (SQLException e) {
            System.err.println("Erreur countByStatut : " + e.getMessage());
        }
        return stats;
    }

    public String detecterPriorite(String sujet, String description) {
        String texte = ((sujet != null ? sujet : "") + " " + (description != null ? description : "")).toLowerCase();
        List<String> motsHaute = List.of(
                "urgence", "urgent", "danger", "blessé", "blessure", "accident", "grave", "critique",
                "immédiat", "secours", "mort", "décès", "sang", "fracture", "perdu", "disparu");
        List<String> motsMoyenne = List.of(
                "problème", "souci", "plainte", "insatisfait", "malade", "maladie", "inquiet", "retard", "refus");
        for (String mot : motsHaute) {
            if (texte.contains(mot)) return "haute";
        }
        for (String mot : motsMoyenne) {
            if (texte.contains(mot)) return "moyenne";
        }
        return "basse";
    }

    private static Reclamation mapRow(ResultSet rs) throws SQLException {
        Reclamation r = new Reclamation();
        r.setId(rs.getInt("id"));
        r.setSujet(rs.getString("sujet"));
        r.setDescription(rs.getString("description"));
        Timestamp ts = rs.getTimestamp("date_reclamation");
        if (ts != null) r.setDateReclamation(ts.toLocalDateTime());
        r.setStatut(rs.getString("statut"));
        r.setPriorite(rs.getString("priorite"));
        r.setNomClient(rs.getString("nom_client"));
        r.setEmailClient(rs.getString("email_client"));
        r.setUserId(rs.getInt("user_id"));
        r.setPhotoUrl(rs.getString("photo_url"));
        return r;
    }
}
