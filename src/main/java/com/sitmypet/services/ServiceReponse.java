package com.sitmypet.services;

import com.sitmypet.model.Reclamation;
import com.sitmypet.model.Reponse;
import com.sitmypet.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReponse {

    private final Connection cnx;

    public ServiceReponse() {
        this.cnx = MyDatabase.getInstance().getConnection();
    }

    public void add(Reponse reponse) {
        if (reponse.getContenu() == null || reponse.getContenu().trim().isEmpty())
            throw new IllegalArgumentException("Le contenu de la réponse ne peut pas être vide.");

        try {
            String req = "INSERT INTO reponse (contenu, date_reponse, auteur, reclamation_id) VALUES (?,?,?,?)";
            try (PreparedStatement ps = cnx.prepareStatement(req)) {
                ps.setString(1, reponse.getContenu().trim());
                ps.setTimestamp(2, Timestamp.valueOf(
                        reponse.getDateReponse() != null ? reponse.getDateReponse() : java.time.LocalDateTime.now()));
                ps.setString(3, reponse.getAuteur());
                ps.setInt(4, reponse.getReclamationId());
                ps.executeUpdate();
            }
            try (PreparedStatement psUpdate = cnx.prepareStatement(
                    "UPDATE reclamation SET statut = 'resolue' WHERE id = ?")) {
                psUpdate.setInt(1, reponse.getReclamationId());
                psUpdate.executeUpdate();
            }

            ServiceReclamation sr = new ServiceReclamation();
            Reclamation rec = sr.getById(reponse.getReclamationId());
            if (rec != null && rec.getEmailClient() != null && !rec.getEmailClient().isEmpty()) {
                Thread mailThread = new Thread(() -> {
                    try {
                        new EmailService().envoyerReponseReclamation(rec.getEmailClient(), rec.getSujet(), reponse.getContenu());
                    } catch (Exception e) {
                        System.err.println("Email réclamation non envoyé : " + e.getMessage());
                    }
                }, "smtp-reclamation-reply");
                mailThread.setDaemon(true);
                mailThread.start();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur insertion réponse : " + e.getMessage(), e);
        }
    }

    public List<Reponse> getByReclamationId(int reclamationId) {
        List<Reponse> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT * FROM reponse WHERE reclamation_id = ? ORDER BY date_reponse ASC")) {
            ps.setInt(1, reclamationId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Erreur getByReclamationId réponse : " + e.getMessage());
        }
        return list;
    }

    public boolean existsForReclamation(int reclamationId) {
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT COUNT(*) FROM reponse WHERE reclamation_id = ?")) {
            ps.setInt(1, reclamationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Erreur existsForReclamation : " + e.getMessage());
        }
        return false;
    }

    private static Reponse mapRow(ResultSet rs) throws SQLException {
        Reponse r = new Reponse();
        r.setId(rs.getInt("id"));
        r.setContenu(rs.getString("contenu"));
        Timestamp ts = rs.getTimestamp("date_reponse");
        if (ts != null) {
            r.setDateReponse(ts.toLocalDateTime());
        }
        r.setAuteur(rs.getString("auteur"));
        r.setReclamationId(rs.getInt("reclamation_id"));
        return r;
    }
}
