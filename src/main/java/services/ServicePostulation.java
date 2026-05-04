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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Ajouter et retourner l'ID généré ──────────────────────
    public int add(Postulation p) {
        String req = "INSERT INTO postulation "
                + "(announcement_id, gardien_id, date_postulation, statut) "
                + "VALUES (?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            pstm.setInt(1, p.getAnnouncementId());
            pstm.setInt(2, p.getGardienId());
            pstm.setDate(3, p.getDatePostulation());
            pstm.setString(4, p.getStatut());
            pstm.executeUpdate();
            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // ── Supprimer ─────────────────────────────────────────────
    public void delete(Postulation p) {
        String req = "DELETE FROM postulation WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, p.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Récupérer toutes les postulations ────────────────────
    public List<Postulation> getAll() {
        List<Postulation> list = new ArrayList<>();
        String req = "SELECT * FROM postulation ORDER BY date_postulation DESC";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                Postulation post = new Postulation();
                post.setId(rs.getInt("id"));
                post.setAnnouncementId(rs.getInt("announcement_id"));
                post.setGardienId(rs.getInt("gardien_id"));
                post.setDatePostulation(rs.getDate("date_postulation"));
                post.setStatut(rs.getString("statut"));
                list.add(post);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── Récupérer par gardien ─────────────────────────────────
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
            e.printStackTrace();
        }
        return list;
    }

    // ── Récupérer par ID ──────────────────────────────────────
    public Postulation getById(int id) {
        String req = "SELECT * FROM postulation WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                Postulation post = new Postulation();
                post.setId(rs.getInt("id"));
                post.setAnnouncementId(rs.getInt("announcement_id"));
                post.setGardienId(rs.getInt("gardien_id"));
                post.setDatePostulation(rs.getDate("date_postulation"));
                post.setStatut(rs.getString("statut"));
                return post;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
            e.printStackTrace();
        }
        return false;
    }

    // ── Mettre à jour le statut ───────────────────────────────
    public void updateStatut(int postulationId, String statut) {
        String req = "UPDATE postulation SET statut = ? WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, statut);
            pstm.setInt(2, postulationId);
            pstm.executeUpdate();
            System.out.println("Statut postulation #" + postulationId + " -> " + statut);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}