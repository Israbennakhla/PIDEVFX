package services;

import interfaces.IService;
import model.Reponse;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReponse implements IService<Reponse> {

    private final Connection cnx = MyDataBase.getInstance().getCnx();

    @Override
    public void add(Reponse r) {
        String sql = "INSERT INTO reponse (contenu, date_reponse, auteur, reclamation_id) " +
                "VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, r.getContenu());
            ps.setTimestamp(2, Timestamp.valueOf(r.getDateReponse()));
            ps.setString(3, r.getAuteur());
            ps.setInt(4, r.getReclamationId());
            ps.executeUpdate();
            System.out.println("✅ Réponse ajoutée !");
        } catch (SQLException e) {
            System.out.println("Erreur ajout réponse : " + e.getMessage());
        }
    }

    @Override
    public List<Reponse> getAll() {
        List<Reponse> list = new ArrayList<>();
        try {
            ResultSet rs = cnx.createStatement()
                    .executeQuery("SELECT * FROM reponse");
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Erreur getAll réponse : " + e.getMessage());
        }
        return list;
    }

    public List<Reponse> getByReclamationId(int reclamationId) {
        List<Reponse> list = new ArrayList<>();
        try {
            PreparedStatement ps = cnx.prepareStatement(
                    "SELECT * FROM reponse WHERE reclamation_id = ?"
            );
            ps.setInt(1, reclamationId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Erreur getByReclamationId : " + e.getMessage());
        }
        return list;
    }

    @Override
    public void update(Reponse r) {
        try {
            PreparedStatement ps = cnx.prepareStatement(
                    "UPDATE reponse SET contenu=?, date_reponse=?, auteur=? WHERE id=?"
            );
            ps.setString(1, r.getContenu());
            ps.setTimestamp(2, Timestamp.valueOf(r.getDateReponse()));
            ps.setString(3, r.getAuteur());
            ps.setInt(4, r.getId());
            ps.executeUpdate();
            System.out.println("✅ Réponse modifiée !");
        } catch (SQLException e) {
            System.out.println("Erreur update réponse : " + e.getMessage());
        }
    }

    @Override
    public void delete(Reponse r) {
        try {
            PreparedStatement ps = cnx.prepareStatement(
                    "DELETE FROM reponse WHERE id = ?"
            );
            ps.setInt(1, r.getId());
            ps.executeUpdate();
            System.out.println("✅ Réponse supprimée !");
        } catch (SQLException e) {
            System.out.println("Erreur delete réponse : " + e.getMessage());
        }
    }

    private Reponse mapRow(ResultSet rs) throws SQLException {
        Reponse r = new Reponse();
        r.setId(rs.getInt("id"));
        r.setContenu(rs.getString("contenu"));
        r.setDateReponse(rs.getTimestamp("date_reponse").toLocalDateTime());
        r.setAuteur(rs.getString("auteur"));
        r.setReclamationId(rs.getInt("reclamation_id"));
        return r;
    }
}