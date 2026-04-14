package services;

import model.EventParticipant;
import utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceEventParticipant {
    private Connection cnx;

    public ServiceEventParticipant() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    // 1. CREATE (Inscription d'un utilisateur à un événement)
    public void add(EventParticipant ep) {
        String req = "INSERT INTO event_participants (event_id, user_id) VALUES (?, ?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, ep.getEventId());
            pst.setInt(2, ep.getUserId());
            pst.executeUpdate();
            System.out.println("Participant ajouté avec succès !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // 2. READ (Afficher tous les participants de tous les événements)
    public List<EventParticipant> getAll() {
        List<EventParticipant> list = new ArrayList<>();
        String req = "SELECT * FROM event_participants";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while(rs.next()) {
                list.add(new EventParticipant(rs.getInt("event_id"), rs.getInt("user_id")));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    // 3. DELETE (Désinscription)
    public void delete(EventParticipant ep) {
        String req = "DELETE FROM event_participants WHERE event_id = ? AND user_id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, ep.getEventId());
            pst.setInt(2, ep.getUserId());
            pst.executeUpdate();
            System.out.println("Participant supprimé (Désinscrit) !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    // 4. CHECK (Vérifier si un utilisateur est déjà inscrit à un événement)
    public boolean isEnrolled(int eventId, int userId) {
        String req = "SELECT COUNT(*) FROM event_participants WHERE event_id = ? AND user_id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, eventId);
            pst.setInt(2, userId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    // Note : UPDATE n'est pas nécessaire pour une table d'association (on ne modifie pas une inscription, on l'ajoute ou on l'annule).
}
