package com.sitmypet.services;

import com.sitmypet.interfaces.IService;
import com.sitmypet.model.Reclamation;
import com.sitmypet.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ServiceReclamation implements IService<Reclamation> {

    private final Connection cnx = MyDataBase.getInstance().getCnx();

    // ── Validation métier ─────────────────────────────────────────────────────
    public String valider(Reclamation r) {
        if (r.getSujet() == null || r.getSujet().trim().isEmpty())
            return "❌ Le sujet est obligatoire.";
        if (r.getSujet().trim().length() < 5)
            return "❌ Le sujet doit contenir au moins 5 caractères.";
        if (r.getDescription() == null || r.getDescription().trim().length() < 10)
            return "❌ La description doit contenir au moins 10 caractères.";
        if (r.getNomClient() == null || r.getNomClient().trim().isEmpty())
            return "❌ Le nom du client est obligatoire.";
        if (r.getEmailClient() == null || !r.getEmailClient().contains("@"))
            return "❌ Email invalide.";
        if (r.getPriorite() == null ||
                !List.of("basse", "moyenne", "haute").contains(r.getPriorite()))
            return "❌ Priorité invalide (basse/moyenne/haute).";
        return "OK";
    }

    // ── Add ───────────────────────────────────────────────────────────────────
    @Override
    public void add(Reclamation r) {
        String validation = valider(r);
        if (!validation.equals("OK")) {
            throw new IllegalArgumentException(validation);
        }

        // Vérification unicité
        try {
            PreparedStatement check = cnx.prepareStatement(
                    "SELECT COUNT(*) FROM reclamation WHERE sujet = ? AND email_client = ?"
            );
            check.setString(1, r.getSujet());
            check.setString(2, r.getEmailClient());
            ResultSet rs = check.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                throw new IllegalArgumentException(
                        "❌ Une réclamation identique existe déjà pour ce client !");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur vérification unicité : " + e.getMessage());
        }

        // Insertion
        String sql = "INSERT INTO reclamation " +
                "(sujet, description, date_reclamation, statut, priorite, " +
                "nom_client, email_client, user_id, photo_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, r.getSujet());
            ps.setString(2, r.getDescription());
            ps.setTimestamp(3, r.getDateReclamation() != null
                    ? Timestamp.valueOf(r.getDateReclamation()) : null);
            ps.setString(4, r.getStatut());
            ps.setString(5, r.getPriorite());
            ps.setString(6, r.getNomClient());
            ps.setString(7, r.getEmailClient());
            ps.setInt(8, r.getUserId());
            ps.setString(9, r.getPhotoUrl());   // null accepté par la DB
            ps.executeUpdate();
            System.out.println("✅ Réclamation ajoutée !");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur ajout : " + e.getMessage());
        }
    }

    // ── GetAll ────────────────────────────────────────────────────────────────
    @Override
    public List<Reclamation> getAll() {
        List<Reclamation> list = new ArrayList<>();
        try {
            ResultSet rs = cnx.createStatement().executeQuery(
                    "SELECT * FROM reclamation " +
                            "ORDER BY FIELD(statut, 'en_attente', 'en_cours', 'resolue'), " +
                            "date_reclamation DESC"
            );
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Erreur getAll : " + e.getMessage());
        }
        return list;
    }

    // ── Update ────────────────────────────────────────────────────────────────
    @Override
    public void update(Reclamation r) {
        String validation = valider(r);
        if (!validation.equals("OK")) {
            throw new IllegalArgumentException(validation);
        }

        String sql = "UPDATE reclamation SET sujet=?, description=?, date_reclamation=?, " +
                "statut=?, priorite=?, nom_client=?, email_client=?, photo_url=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, r.getSujet());
            ps.setString(2, r.getDescription());
            ps.setTimestamp(3, r.getDateReclamation() != null
                    ? Timestamp.valueOf(r.getDateReclamation()) : null);
            ps.setString(4, r.getStatut());
            ps.setString(5, r.getPriorite());
            ps.setString(6, r.getNomClient());
            ps.setString(7, r.getEmailClient());
            ps.setString(8, r.getPhotoUrl());
            ps.setInt(9, r.getId());
            ps.executeUpdate();
            System.out.println("✅ Réclamation modifiée !");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update : " + e.getMessage());
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────
    @Override
    public void delete(Reclamation r) {
        try {
            PreparedStatement ps = cnx.prepareStatement(
                    "DELETE FROM reclamation WHERE id = ?"
            );
            ps.setInt(1, r.getId());
            ps.executeUpdate();
            System.out.println("✅ Réclamation supprimée !");
        } catch (SQLException e) {
            System.out.println("Erreur delete : " + e.getMessage());
        }
    }

    // ── GetById ───────────────────────────────────────────────────────────────
    public Reclamation getById(int id) throws Exception {
        PreparedStatement ps = cnx.prepareStatement(
                "SELECT * FROM reclamation WHERE id = ?"
        );
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return mapRow(rs);
        return null;
    }

    // ── Recherches / Filtres ──────────────────────────────────────────────────
    public List<Reclamation> searchByNom(String nom) {
        List<Reclamation> list = new ArrayList<>();
        try {
            PreparedStatement ps = cnx.prepareStatement(
                    "SELECT * FROM reclamation WHERE nom_client LIKE ?"
            );
            ps.setString(1, "%" + nom + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Erreur searchByNom : " + e.getMessage());
        }
        return list;
    }

    public List<Reclamation> searchByStatutAndPriorite(String statut, String priorite) {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation WHERE 1=1";
        if (statut != null && !statut.equals("Tous"))
            sql += " AND statut = '" + statut + "'";
        if (priorite != null && !priorite.equals("Tous"))
            sql += " AND priorite = '" + priorite + "'";
        try {
            ResultSet rs = cnx.createStatement().executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Erreur searchByStatutAndPriorite : " + e.getMessage());
        }
        return list;
    }

    public List<Reclamation> searchByNomStatutPriorite(String nom, String statut, String priorite) {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation WHERE nom_client LIKE ?";
        if (statut != null && !statut.equals("Tous"))
            sql += " AND statut = '" + statut + "'";
        if (priorite != null && !priorite.equals("Tous"))
            sql += " AND priorite = '" + priorite + "'";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, "%" + nom + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Erreur searchByNomStatutPriorite : " + e.getMessage());
        }
        return list;
    }

    public List<Reclamation> getByPriorite(String priorite) {
        List<Reclamation> list = new ArrayList<>();
        try {
            PreparedStatement ps = cnx.prepareStatement(
                    "SELECT * FROM reclamation WHERE priorite = ? ORDER BY date_reclamation DESC"
            );
            ps.setString(1, priorite);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Erreur getByPriorite : " + e.getMessage());
        }
        return list;
    }

    // ── Statut ────────────────────────────────────────────────────────────────
    public void changerStatut(int id, String nouveauStatut) {
        if (!List.of("en_attente", "en_cours", "resolue").contains(nouveauStatut)) {
            System.out.println("❌ Statut invalide : " + nouveauStatut);
            return;
        }
        try {
            PreparedStatement ps = cnx.prepareStatement(
                    "UPDATE reclamation SET statut = ? WHERE id = ?"
            );
            ps.setString(1, nouveauStatut);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur changerStatut : " + e.getMessage());
        }
    }

    // ── Stats dashboard — clés minuscules = cohérentes avec la DB ─────────────
    public Map<String, Integer> countByStatut() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("en_attente", 0);   // ← minuscules comme dans la DB
        stats.put("en_cours",   0);
        stats.put("resolue",    0);
        try {
            ResultSet rs = cnx.createStatement().executeQuery(
                    "SELECT statut, COUNT(*) AS total FROM reclamation GROUP BY statut"
            );
            while (rs.next()) {
                String statut = rs.getString("statut");
                int count     = rs.getInt("total");
                if (stats.containsKey(statut)) stats.put(statut, count);
            }
        } catch (SQLException e) {
            System.out.println("Erreur countByStatut : " + e.getMessage());
        }
        return stats;
    }

    // ── Méthode count simple (garde pour compatibilité) ───────────────────────
    public int countByStatut(String statut) {
        try {
            PreparedStatement ps = cnx.prepareStatement(
                    "SELECT COUNT(*) FROM reclamation WHERE statut = ?"
            );
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Erreur count : " + e.getMessage());
            return 0;
        }
    }

    // ── MapRow ────────────────────────────────────────────────────────────────
    private Reclamation mapRow(ResultSet rs) throws SQLException {
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
    public String detecterPriorite(String sujet, String description) {
        String texte = (sujet + " " + description).toLowerCase();

        List<String> motsHaute = List.of(
                "urgence", "urgent", "danger", "blessé", "blessure",
                "accident", "grave", "critique", "immédiat", "secours",
                "mort", "décès", "sang", "fracture", "perdu", "disparu"
        );

        List<String> motsMoyenne = List.of(
                "problème", "souci", "plainte", "insatisfait",
                "malade", "maladie", "inquiet", "retard", "refus"
        );

        for (String mot : motsHaute) {
            if (texte.contains(mot)) return "haute";
        }
        for (String mot : motsMoyenne) {
            if (texte.contains(mot)) return "moyenne";
        }
        return "basse";
    }
}