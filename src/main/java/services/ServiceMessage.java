package services;

import model.Message;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceMessage {

    private Connection cnx;

    public ServiceMessage() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS message ("
                + "id               INT AUTO_INCREMENT PRIMARY KEY,"
                + "expediteur_id    INT      NOT NULL,"
                + "destinataire_id  INT      NOT NULL,"
                + "postulation_id   INT      NOT NULL,"
                + "contenu          TEXT     NOT NULL,"
                + "date_envoi       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "lu               BOOLEAN  NOT NULL DEFAULT FALSE"
                + ")";
        try { cnx.createStatement().executeUpdate(sql); }
        catch (SQLException e) { e.printStackTrace(); }
    }

    // ── Envoyer un message ────────────────────────────────────
    public void envoyer(Message m) {
        String sql = "INSERT INTO message "
                + "(expediteur_id, destinataire_id, postulation_id, contenu) "
                + "VALUES (?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, m.getExpediteurId());
            ps.setInt(2, m.getDestinataireId());
            ps.setInt(3, m.getPostulationId());
            ps.setString(4, m.getContenu());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── Récupérer tous les messages d'une conversation ────────
    public List<Message> getConversation(int postulationId) {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT * FROM message WHERE postulation_id = ? ORDER BY date_envoi ASC";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, postulationId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Message m = new Message();
                m.setId(rs.getInt("id"));
                m.setExpediteurId(rs.getInt("expediteur_id"));
                m.setDestinataireId(rs.getInt("destinataire_id"));
                m.setPostulationId(rs.getInt("postulation_id"));
                m.setContenu(rs.getString("contenu"));
                m.setDateEnvoi(rs.getTimestamp("date_envoi"));
                m.setLu(rs.getBoolean("lu"));
                list.add(m);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── Récupérer nouveaux messages depuis un ID donné ────────
    public List<Message> getNouveauxDepuis(int postulationId, int dernierIdConnu) {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT * FROM message WHERE postulation_id = ? AND id > ? ORDER BY date_envoi ASC";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, postulationId);
            ps.setInt(2, dernierIdConnu);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Message m = new Message();
                m.setId(rs.getInt("id"));
                m.setExpediteurId(rs.getInt("expediteur_id"));
                m.setDestinataireId(rs.getInt("destinataire_id"));
                m.setPostulationId(rs.getInt("postulation_id"));
                m.setContenu(rs.getString("contenu"));
                m.setDateEnvoi(rs.getTimestamp("date_envoi"));
                m.setLu(rs.getBoolean("lu"));
                list.add(m);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}