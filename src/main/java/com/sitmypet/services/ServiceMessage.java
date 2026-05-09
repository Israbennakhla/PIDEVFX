package com.sitmypet.services;

import com.sitmypet.model.Message;
import com.sitmypet.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceMessage {

    private final Connection cnx;

    public ServiceMessage() {
        this.cnx = MyDatabase.getInstance().getConnection();
    }

    public void envoyer(Message m) {
        String sql = "INSERT INTO message "
                + "(expediteur_id, destinataire_id, postulation_id, contenu) "
                + "VALUES (?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, m.getExpediteurId());
            ps.setInt(2, m.getDestinataireId());
            ps.setInt(3, m.getPostulationId());
            ps.setString(4, m.getContenu());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Message> getConversation(int postulationId) {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT * FROM message WHERE postulation_id = ? ORDER BY date_envoi ASC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, postulationId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Message> getNouveauxDepuis(int postulationId, int dernierIdConnu) {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT * FROM message WHERE postulation_id = ? AND id > ? ORDER BY date_envoi ASC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, postulationId);
            ps.setInt(2, dernierIdConnu);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Message mapRow(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setId(rs.getInt("id"));
        m.setExpediteurId(rs.getInt("expediteur_id"));
        m.setDestinataireId(rs.getInt("destinataire_id"));
        m.setPostulationId(rs.getInt("postulation_id"));
        m.setContenu(rs.getString("contenu"));
        m.setDateEnvoi(rs.getTimestamp("date_envoi"));
        m.setLu(rs.getBoolean("lu"));
        return m;
    }
}
