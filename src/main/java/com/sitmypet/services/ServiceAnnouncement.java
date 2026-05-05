package com.sitmypet.services;

import com.sitmypet.model.Announcement;
import com.sitmypet.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceAnnouncement {

    private final Connection cnx;

    public ServiceAnnouncement() {
        this.cnx = MyDatabase.getInstance().getConnection();
    }

    public void add(Announcement a) {
        String req = "INSERT INTO announcement "
                + "(address, visit_hours, care_type, date_debut, date_fin, "
                + "visit_per_day, renumeration_min, renumeration_max, services, pet_id, user_id) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pstm = cnx.prepareStatement(req)) {
            pstm.setString(1, a.getAddress() != null ? a.getAddress() : "");
            pstm.setString(2, toJsonArray(a.getVisitHours()));
            pstm.setString(3, a.getCareType() != null ? a.getCareType() : "EN_CHENIL");
            pstm.setDate(4, a.getDateDebut());
            pstm.setDate(5, a.getDateFin());
            pstm.setInt(6, a.getVisitPerDay());
            pstm.setFloat(7, a.getRemunerationMin());
            pstm.setFloat(8, a.getRemunerationMax());
            pstm.setString(9, a.getServices() != null ? a.getServices() : "");
            pstm.setInt(10, a.getPetId());
            pstm.setInt(11, a.getUserId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public List<Announcement> getAll() {
        List<Announcement> list = new ArrayList<>();
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM announcement")) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur getAll announcement : " + e.getMessage());
        }
        return list;
    }

    public Announcement getById(int id) {
        String sql = "SELECT * FROM announcement WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur getById announcement : " + e.getMessage());
        }
        return null;
    }

    public List<Announcement> getByUserId(int userId) {
        List<Announcement> list = new ArrayList<>();
        String sql = "SELECT * FROM announcement WHERE user_id = ? ORDER BY id DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur getByUserId announcement : " + e.getMessage());
        }
        return list;
    }

    /** Annonces visibles par un gardien : toutes sauf celles déjà « gagnées » par ce gardien. */
    public List<Announcement> getOpenForGardien(int gardienId) {
        List<Announcement> list = new ArrayList<>();
        String sql = "SELECT a.* FROM announcement a WHERE a.id NOT IN ("
                + "SELECT announcement_id FROM postulation WHERE gardien_id = ? AND statut = 'ACCEPTE'"
                + ") ORDER BY a.id DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, gardienId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur getOpenForGardien : " + e.getMessage());
        }
        return list;
    }

    public void update(Announcement a) {
        String req = "UPDATE announcement SET address=?, visit_hours=?, care_type=?, "
                + "date_debut=?, date_fin=?, visit_per_day=?, renumeration_min=?, "
                + "renumeration_max=?, services=?, pet_id=?, user_id=? WHERE id=? AND user_id=?";
        try (PreparedStatement pstm = cnx.prepareStatement(req)) {
            pstm.setString(1, a.getAddress() != null ? a.getAddress() : "");
            pstm.setString(2, toJsonArray(a.getVisitHours()));
            pstm.setString(3, a.getCareType() != null ? a.getCareType() : "EN_CHENIL");
            pstm.setDate(4, a.getDateDebut());
            pstm.setDate(5, a.getDateFin());
            pstm.setInt(6, a.getVisitPerDay());
            pstm.setFloat(7, a.getRemunerationMin());
            pstm.setFloat(8, a.getRemunerationMax());
            pstm.setString(9, a.getServices() != null ? a.getServices() : "");
            pstm.setInt(10, a.getPetId());
            pstm.setInt(11, a.getUserId());
            pstm.setInt(12, a.getId());
            pstm.setInt(13, a.getUserId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void delete(Announcement a) {
        try (PreparedStatement pstm = cnx.prepareStatement("DELETE FROM announcement WHERE id=? AND user_id=?")) {
            pstm.setInt(1, a.getId());
            pstm.setInt(2, a.getUserId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Announcement mapRow(ResultSet rs) throws SQLException {
        Announcement a = new Announcement();
        a.setId(rs.getInt("id"));
        a.setAddress(rs.getString("address"));
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
        return a;
    }

    private String toJsonArray(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return "[]";
        }
        if (csv.trim().startsWith("[")) return csv;
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
