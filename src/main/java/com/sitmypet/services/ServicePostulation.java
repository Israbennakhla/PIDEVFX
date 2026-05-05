package com.sitmypet.services;

import com.sitmypet.model.Postulation;
import com.sitmypet.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePostulation {

    private final Connection cnx;

    public ServicePostulation() {
        this.cnx = MyDatabase.getInstance().getConnection();
    }

    public int add(Postulation p) {
        String req = "INSERT INTO postulation "
                + "(announcement_id, gardien_id, date_postulation, statut) "
                + "VALUES (?,?,?,?)";
        try (PreparedStatement pstm = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
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

    public void delete(Postulation p) {
        String req = "DELETE FROM postulation WHERE id = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(req)) {
            pstm.setInt(1, p.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Postulation> getByGardien(int gardienId) {
        List<Postulation> list = new ArrayList<>();
        String req = "SELECT * FROM postulation WHERE gardien_id = ? ORDER BY date_postulation DESC";
        try (PreparedStatement pstm = cnx.prepareStatement(req)) {
            pstm.setInt(1, gardienId);
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Postulations reçues sur les annonces d'un propriétaire. */
    public List<Postulation> getByProprietaireId(int proprietaireUserId) {
        List<Postulation> list = new ArrayList<>();
        String sql = "SELECT p.* FROM postulation p "
                + "INNER JOIN announcement a ON p.announcement_id = a.id "
                + "WHERE a.user_id = ? ORDER BY p.date_postulation DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, proprietaireUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Liste complète pour le panneau administrateur. */
    public List<Postulation> getAll() {
        List<Postulation> list = new ArrayList<>();
        String req = "SELECT * FROM postulation ORDER BY date_postulation DESC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur getAll postulation : " + e.getMessage());
        }
        return list;
    }

    public Postulation getById(int id) {
        String req = "SELECT * FROM postulation WHERE id = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(req)) {
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean hasPostule(int gardienId, int announcementId) {
        String req = "SELECT COUNT(*) FROM postulation WHERE gardien_id = ? AND announcement_id = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(req)) {
            pstm.setInt(1, gardienId);
            pstm.setInt(2, announcementId);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateStatut(int postulationId, String statut) {
        String req = "UPDATE postulation SET statut = ? WHERE id = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(req)) {
            pstm.setString(1, statut);
            pstm.setInt(2, postulationId);
            pstm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Postulations acceptées où l'utilisateur est gardien ou propriétaire de l'annonce. */
    public List<Postulation> getAccepteesInvolvingUser(int userId) {
        List<Postulation> out = new ArrayList<>();
        String sql = "SELECT p.* FROM postulation p "
                + "INNER JOIN announcement a ON p.announcement_id = a.id "
                + "WHERE p.statut = 'ACCEPTE' AND (p.gardien_id = ? OR a.user_id = ?) "
                + "ORDER BY p.date_postulation DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                out.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    private Postulation mapRow(ResultSet rs) throws SQLException {
        Postulation post = new Postulation();
        post.setId(rs.getInt("id"));
        post.setAnnouncementId(rs.getInt("announcement_id"));
        post.setGardienId(rs.getInt("gardien_id"));
        post.setDatePostulation(rs.getDate("date_postulation"));
        post.setStatut(rs.getString("statut"));
        return post;
    }
}
