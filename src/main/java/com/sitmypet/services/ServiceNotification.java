package com.sitmypet.services;

import com.sitmypet.model.Notification;
import com.sitmypet.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceNotification {

    private Connection cnx;

    public ServiceNotification() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS notification ("
                + "id              INT AUTO_INCREMENT PRIMARY KEY,"
                + "destinataire_id INT         NOT NULL,"
                + "expediteur_id   INT         NOT NULL,"
                + "postulation_id  INT         NOT NULL,"
                + "message         TEXT        NOT NULL,"
                + "type            VARCHAR(30) NOT NULL,"
                + "lu              BOOLEAN     NOT NULL DEFAULT FALSE,"
                + "date_creation   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP"
                + ")";
        try {
            cnx.createStatement().executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Envoyer une notification ──────────────────────────────
    public void envoyer(Notification n) {
        String sql = "INSERT INTO notification "
                + "(destinataire_id, expediteur_id, postulation_id, message, type) "
                + "VALUES (?,?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
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

    // ── Récupérer les notifs non lues d'un utilisateur ────────
    public List<Notification> getNonLues(int userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notification WHERE destinataire_id=? AND lu=FALSE "
                + "ORDER BY date_creation DESC";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Notification n = new Notification();
                n.setId(rs.getInt("id"));
                n.setDestinataireId(rs.getInt("destinataire_id"));
                n.setExpediteurId(rs.getInt("expediteur_id"));
                n.setPostulationId(rs.getInt("postulation_id"));
                n.setMessage(rs.getString("message"));
                n.setType(rs.getString("type"));
                n.setLu(rs.getBoolean("lu"));
                n.setDateCreation(rs.getTimestamp("date_creation"));
                list.add(n);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── Marquer une notification comme lue ────────────────────
    public void marquerLue(int notifId) {
        try {
            PreparedStatement ps = cnx.prepareStatement(
                    "UPDATE notification SET lu=TRUE WHERE id=?");
            ps.setInt(1, notifId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Marquer toutes lues pour un user ──────────────────────
    public void marquerToutesLues(int userId) {
        try {
            PreparedStatement ps = cnx.prepareStatement(
                    "UPDATE notification SET lu=TRUE WHERE destinataire_id=?");
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Mettre à jour statut postulation ─────────────────────
    public void updateStatutPostulation(int postulationId, String statut) {
        try {
            PreparedStatement ps = cnx.prepareStatement(
                    "UPDATE postulation SET statut=? WHERE id=?");
            ps.setString(1, statut);
            ps.setInt(2, postulationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}