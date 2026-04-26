package services;

import model.Postulation;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePostulation {

    private Connection cnx;

    public ServicePostulation() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    // ── Créer la table si elle n'existe pas ──────────────────
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS postulation ("
                + "id               INT AUTO_INCREMENT PRIMARY KEY,"
                + "announcement_id  INT         NOT NULL,"
                + "gardien_id       INT         NOT NULL,"
                + "date_postulation DATE        NOT NULL,"
                + "statut           VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE'"
                + ")";
        try {
            Statement st = cnx.createStatement();
            st.executeUpdate(sql);
            System.out.println("Table postulation OK");
        } catch (SQLException e) {
            System.out.println("Erreur createTable : " + e.getMessage());
        }
    }

    // ── Ajouter une postulation ───────────────────────────────
    public void add(Postulation p) {
        String req = "INSERT INTO postulation "
                + "(announcement_id, gardien_id, date_postulation, statut) "
                + "VALUES (?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, p.getAnnouncementId());
            pstm.setInt(2, p.getGardienId());
            pstm.setDate(3, p.getDatePostulation());
            pstm.setString(4, p.getStatut());
            pstm.executeUpdate();
            System.out.println("Postulation ajoutee !");
        } catch (SQLException e) {
            System.out.println("Erreur add postulation : " + e.getMessage());
        }
    }

    // ── Supprimer une postulation ─────────────────────────────
    public void delete(Postulation p) {
        String req = "DELETE FROM postulation WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, p.getId());
            pstm.executeUpdate();
            System.out.println("Postulation supprimee (id=" + p.getId() + ")");
        } catch (SQLException e) {
            System.out.println("Erreur delete postulation : " + e.getMessage());
        }
    }

    // ── Récupérer les postulations d'un gardien ───────────────
    public List<Postulation> getByGardien(int gardienId) {
        List<Postulation> list = new ArrayList<>();
        String req = "SELECT * FROM postulation WHERE gardien_id = ? "
                + "ORDER BY date_postulation DESC";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, gardienId);
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                Postulation post = new Postulation();
                post.setId(rs.getInt("id"));
                post.setAnnouncementId(rs.getInt("announcement_id"));
                post.setGardienId(rs.getInt("gardien_id"));
                post.setDatePostulation(rs.getDate("date_postulation"));
                post.setStatut(rs.getString("statut"));
                list.add(post);
            }
        } catch (SQLException e) {
            System.out.println("Erreur getByGardien : " + e.getMessage());
        }
        return list;
    }

    // ── Vérifier si déjà postulé ──────────────────────────────
    public boolean hasPostule(int gardienId, int announcementId) {
        String req = "SELECT COUNT(*) FROM postulation "
                + "WHERE gardien_id = ? AND announcement_id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, gardienId);
            pstm.setInt(2, announcementId);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.out.println("Erreur hasPostule : " + e.getMessage());
        }
        return false;
    }
}