package services;

import interfaces.IService;
import model.Reclamation;
import model.Reponse;
import utils.EmailService;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReponse implements IService<Reponse> {

    private final Connection cnx = MyDataBase.getInstance().getCnx();

    @Override
    public void add(Reponse reponse) {
        if (reponse.getContenu() == null || reponse.getContenu().trim().isEmpty()) {
            throw new IllegalArgumentException("Le contenu de la réponse ne peut pas être vide.");
        }

        try {
            String req = "INSERT INTO reponse (contenu, date_reponse, auteur, reclamation_id) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, reponse.getContenu());
            ps.setTimestamp(2, Timestamp.valueOf(reponse.getDateReponse()));
            ps.setString(3, reponse.getAuteur());
            ps.setInt(4, reponse.getReclamationId());
            ps.executeUpdate();

            ServiceReclamation sr = new ServiceReclamation();
            Reclamation rec = sr.getById(reponse.getReclamationId());

            if (rec != null && rec.getEmailClient() != null && !rec.getEmailClient().isEmpty()) {
                new Thread(() -> {
                    try {
                        EmailService.envoyerReponse(
                                rec.getEmailClient(),
                                rec.getSujet(),
                                reponse.getContenu()
                        );
                    } catch (RuntimeException e) {
                        System.err.println("Email non envoyé : " + e.getMessage());
                    }
                }).start();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur insertion réponse : " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Erreur récupération réclamation : " + e.getMessage());
        }
    }

    @Override
    public List<Reponse> getAll() {
        List<Reponse> list = new ArrayList<>();
        try {
            ResultSet rs = cnx.createStatement().executeQuery("SELECT * FROM reponse");
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
            PreparedStatement ps = cnx.prepareStatement("DELETE FROM reponse WHERE id = ?");
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