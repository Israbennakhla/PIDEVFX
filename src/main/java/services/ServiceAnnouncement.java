package services;

import model.Announcement;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceAnnouncement {

    private final Connection cnx;

    public ServiceAnnouncement() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    // ── Ajouter ───────────────────────────────────────────────
    public void add(Announcement a) {
        String req = "INSERT INTO announcement " +
                "(address, visit_hours, care_type, date_debut, date_fin, " +
                "visit_per_day, renumeration_min, renumeration_max, services, pet_id, user_id) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, a.getAddress() != null ? a.getAddress() : "");
            // ✅ visit_hours est JSON → convertir en tableau JSON  ["08:00","12:00"]
            pstm.setString(2, toJsonArray(a.getVisitHours()));
            pstm.setString(3, a.getCareType() != null ? a.getCareType() : "EN_CHENIL");
            pstm.setDate(4,   a.getDateDebut());
            pstm.setDate(5,   a.getDateFin());
            pstm.setInt(6,    a.getVisitPerDay());
            pstm.setFloat(7,  a.getRemunerationMin());
            pstm.setFloat(8,  a.getRemunerationMax());
            pstm.setString(9, a.getServices() != null ? a.getServices() : "");
            pstm.setInt(10,   a.getPetId());
            pstm.setInt(11,   a.getUserId());

            int rows = pstm.executeUpdate();
            System.out.println("✅ Annonce insérée — lignes : " + rows);

        } catch (SQLException e) {
            System.out.println("❌ Erreur SQL add : " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    // ── Lire toutes ───────────────────────────────────────────
    public List<Announcement> getAll() {
        List<Announcement> list = new ArrayList<>();
        try {
            ResultSet rs = cnx.createStatement().executeQuery("SELECT * FROM announcement");
            while (rs.next()) {
                Announcement a = new Announcement();
                a.setId(rs.getInt("id"));
                a.setAddress(rs.getString("address"));
                // ✅ visit_hours revient comme JSON string → on le stocke tel quel
                a.setVisitHours(rs.getString("visit_hours"));
                a.setCareType(rs.getString("care_type"));
                a.setDateDebut(rs.getDate("date_debut"));
                a.setDateFin(rs.getDate("date_fin"));
                a.setVisitPerDay(rs.getInt("visit_per_day"));
                a.setRemunerationMin(rs.getFloat("renumeration_min"));
                a.setRemunerationMax(rs.getFloat("renumeration_max"));
                a.setServices(rs.getString("services"));
                a.setPetId(rs.getInt("pet_id"));
                a.setUserId(rs.getInt("user_id"));
                list.add(a);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur getAll : " + e.getMessage());
        }
        return list;
    }

    // ── Modifier ──────────────────────────────────────────────
    public void update(Announcement a) {
        String req = "UPDATE announcement SET address=?, visit_hours=?, care_type=?, " +
                "date_debut=?, date_fin=?, visit_per_day=?, renumeration_min=?, " +
                "renumeration_max=?, services=?, pet_id=?, user_id=? WHERE id=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, a.getAddress() != null ? a.getAddress() : "");
            pstm.setString(2, toJsonArray(a.getVisitHours()));
            pstm.setString(3, a.getCareType() != null ? a.getCareType() : "EN_CHENIL");
            pstm.setDate(4,   a.getDateDebut());
            pstm.setDate(5,   a.getDateFin());
            pstm.setInt(6,    a.getVisitPerDay());
            pstm.setFloat(7,  a.getRemunerationMin());
            pstm.setFloat(8,  a.getRemunerationMax());
            pstm.setString(9, a.getServices() != null ? a.getServices() : "");
            pstm.setInt(10,   a.getPetId());
            pstm.setInt(11,   a.getUserId());
            pstm.setInt(12,   a.getId());
            int rows = pstm.executeUpdate();
            System.out.println("✅ Annonce modifiée (id=" + a.getId() + ") — lignes : " + rows);
        } catch (SQLException e) {
            System.out.println("❌ Erreur update : " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    // ── Supprimer ─────────────────────────────────────────────
    public void delete(Announcement a) {
        try {
            PreparedStatement pstm = cnx.prepareStatement("DELETE FROM announcement WHERE id=?");
            pstm.setInt(1, a.getId());
            int rows = pstm.executeUpdate();
            System.out.println("✅ Annonce supprimée (id=" + a.getId() + ") — lignes : " + rows);
        } catch (SQLException e) {
            System.out.println("❌ Erreur delete : " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    // ── Utilitaire : "08:00,12:00" → ["08:00","12:00"] ───────
    // ✅ Convertit une string CSV en tableau JSON valide pour MySQL
    private String toJsonArray(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return "[]"; // JSON array vide
        }
        // Déjà au format JSON ?
        if (csv.trim().startsWith("[")) return csv;

        // Construire le tableau JSON manuellement
        String[] parts = csv.split(",");
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < parts.length; i++) {
            sb.append("\"").append(parts[i].trim()).append("\"");
            if (i < parts.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}