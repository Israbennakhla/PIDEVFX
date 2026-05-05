package com.sitmypet.services;

import com.sitmypet.model.Notification;
import com.sitmypet.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceNotification {

    private final Connection cnx;

    public ServiceNotification() {
        this.cnx = MyDatabase.getInstance().getConnection();
    }

    public void envoyer(Notification n) {
        String sql = "INSERT INTO notification "
                + "(destinataire_id, expediteur_id, postulation_id, message, type) "
                + "VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, n.getDestinataireId());
            ps.setInt(2, n.getExpediteurId());
            ps.setInt(3, n.getPostulationId());
            ps.setString(4, n.getMessage());
            ps.setString(5, n.getType());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Notification> getNonLues(int userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notification WHERE destinataire_id=? AND lu=FALSE "
                + "ORDER BY date_creation DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void marquerLue(int notifId) {
        try (PreparedStatement ps = cnx.prepareStatement(
                "UPDATE notification SET lu=TRUE WHERE id=?")) {
            ps.setInt(1, notifId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void marquerToutesLues(int userId) {
        try (PreparedStatement ps = cnx.prepareStatement(
                "UPDATE notification SET lu=TRUE WHERE destinataire_id=?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setDestinataireId(rs.getInt("destinataire_id"));
        n.setExpediteurId(rs.getInt("expediteur_id"));
        n.setPostulationId(rs.getInt("postulation_id"));
        n.setMessage(rs.getString("message"));
        n.setType(rs.getString("type"));
        n.setLu(rs.getBoolean("lu"));
        n.setDateCreation(rs.getTimestamp("date_creation"));
        return n;
    }
}
