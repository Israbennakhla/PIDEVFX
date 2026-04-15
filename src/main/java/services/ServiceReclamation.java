package services;

import interfaces.IService;
import model.Reclamation;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReclamation implements IService<Reclamation> {

    private final Connection cnx = MyDataBase.getInstance().getCnx();

    // ══════════════════════════════════════════
    //              VALIDATION MÉTIER
    // ══════════════════════════════════════════
    public String valider(Reclamation r) {
        if (r.getSujet() == null || r.getSujet().trim().isEmpty())
            return "❌ Le sujet est obligatoire.";
        if (r.getSujet().trim().length() < 5)
            return "❌ Le sujet doit contenir au moins 5 caractères.";
        if (r.getDescription() == null || r.getDescription().trim().length() < 10)
            return "❌ La description doit contenir au moins 10 caractères.";
        if (r.getNomClient() == null || r.getNomClient().trim().isEmpty())
            return "❌ Le nom du client est obligatoire.";
        if (r.getEmailClient() == null || !r.getEmailClient().contains("@"))
            return "❌ Email invalide.";
        if (r.getPriorite() == null ||
                !List.of("basse", "moyenne", "haute").contains(r.getPriorite()))
            return "❌ Priorité invalide (basse/moyenne/haute).";
        return "OK";
    }

    // ══════════════════════════════════════════
    //                   ADD
    // ══════════════════════════════════════════
    @Override
    public void add(Reclamation r) {
        // Validation métier
        String validation = valider(r);
        if (!validation.equals("OK")) {
            System.out.println(validation);
            return;
        }

        // Vérification unicité : même sujet + même email
        try {
            PreparedStatement check = cnx.prepareStatement(
                    "SELECT COUNT(*) FROM reclamation WHERE sujet = ? AND email_client = ?"
            );
            check.setString(1, r.getSujet());
            check.setString(2, r.getEmailClient());
            ResultSet rs = check.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                System.out.println("❌ Réclamation identique déjà existante pour ce client !");
                return;
            }
        } catch (SQLException e) {
            System.out.println("Erreur vérification unicité : " + e.getMessage());
            return;
        }

        // Insertion
        String sql = "INSERT INTO reclamation " +
                "(sujet, description, date_reclamation, statut, priorite, nom_client, email_client, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, r.getSujet());
            ps.setString(2, r.getDescription());
            ps.setTimestamp(3, r.getDateReclamation() != null
                    ? Timestamp.valueOf(r.getDateReclamation()) : null);
            ps.setString(4, r.getStatut());
            ps.setString(5, r.getPriorite());
            ps.setString(6, r.getNomClient());
            ps.setString(7, r.getEmailClient());
            ps.setInt(8, r.getUserId());
            ps.executeUpdate();
            System.out.println("✅ Réclamation ajoutée !");
        } catch (SQLException e) {
            System.out.println("Erreur ajout : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════
    //                  GET ALL
    // ══════════════════════════════════════════
    @Override
    public List<Reclamation> getAll() {
        List<Reclamation> list = new ArrayList<>();
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT * FROM reclamation ORDER BY date_reclamation DESC"
            );
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Erreur getAll : " + e.getMessage());
        }
        return list;
    }

    // ══════════════════════════════════════════
    //                  UPDATE
    // ══════════════════════════════════════════
    @Override
    public void update(Reclamation r) {
        // Validation métier
        String validation = valider(r);
        if (!validation.equals("OK")) {
            System.out.println(validation);
            return;
        }

        String sql = "UPDATE reclamation SET sujet=?, description=?, date_reclamation=?, " +
                "statut=?, priorite=?, nom_client=?, email_client=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, r.getSujet());
            ps.setString(2, r.getDescription());
            ps.setTimestamp(3, r.getDateReclamation() != null
                    ? Timestamp.valueOf(r.getDateReclamation()) : null);
            ps.setString(4, r.getStatut());
            ps.setString(5, r.getPriorite());
            ps.setString(6, r.getNomClient());
            ps.setString(7, r.getEmailClient());
            ps.setInt(8, r.getId());
            ps.executeUpdate();
            System.out.println("✅ Réclamation modifiée !");
        } catch (SQLException e) {
            System.out.println("Erreur update : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════
    //                  DELETE
    // ══════════════════════════════════════════
    @Override
    public void delete(Reclamation r) {
        try {
            PreparedStatement ps = cnx.prepareStatement(
                    "DELETE FROM reclamation WHERE id = ?"
            );
            ps.setInt(1, r.getId());
            ps.executeUpdate();
            System.out.println("✅ Réclamation supprimée !");
        } catch (SQLException e) {
            System.out.println("Erreur delete : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════
    //           MÉTHODES SUPPLÉMENTAIRES
    // ══════════════════════════════════════════

    // Recherche par statut et/ou priorité
    public List<Reclamation> searchByStatutAndPriorite(String statut, String priorite) {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation WHERE 1=1";
        if (statut != null && !statut.equals("Tous"))
            sql += " AND statut = '" + statut + "'";
        if (priorite != null && !priorite.equals("Tous"))
            sql += " AND priorite = '" + priorite + "'";
        try {
            ResultSet rs = cnx.createStatement().executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Erreur search : " + e.getMessage());
        }
        return list;
    }

    // Réclamations par priorité
    public List<Reclamation> getByPriorite(String priorite) {
        List<Reclamation> list = new ArrayList<>();
        try {
            PreparedStatement ps = cnx.prepareStatement(
                    "SELECT * FROM reclamation WHERE priorite = ? ORDER BY date_reclamation DESC"
            );
            ps.setString(1, priorite);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Erreur getByPriorite : " + e.getMessage());
        }
        return list;
    }

    // Réclamations par email client
    public List<Reclamation> getByEmail(String email) {
        List<Reclamation> list = new ArrayList<>();
        try {
            PreparedStatement ps = cnx.prepareStatement(
                    "SELECT * FROM reclamation WHERE email_client = ?"
            );
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Erreur getByEmail : " + e.getMessage());
        }
        return list;
    }

    // Changer le statut d'une réclamation
    public void changerStatut(int id, String nouveauStatut) {
        if (!List.of("en_attente", "en_cours", "resolue").contains(nouveauStatut)) {
            System.out.println("❌ Statut invalide : " + nouveauStatut);
            return;
        }
        try {
            PreparedStatement ps = cnx.prepareStatement(
                    "UPDATE reclamation SET statut = ? WHERE id = ?"
            );
            ps.setString(1, nouveauStatut);
            ps.setInt(2, id);
            ps.executeUpdate();
            System.out.println("✅ Statut mis à jour → " + nouveauStatut);
        } catch (SQLException e) {
            System.out.println("Erreur changerStatut : " + e.getMessage());
        }
    }

    // Compter par statut
    public int countByStatut(String statut) {
        try {
            PreparedStatement ps = cnx.prepareStatement(
                    "SELECT COUNT(*) FROM reclamation WHERE statut = ?"
            );
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Erreur count : " + e.getMessage());
            return 0;
        }
    }

    // ══════════════════════════════════════════
    //           MÉTHODE UTILITAIRE MAPPER
    // ══════════════════════════════════════════
    private Reclamation mapRow(ResultSet rs) throws SQLException {
        Reclamation r = new Reclamation();
        r.setId(rs.getInt("id"));
        r.setSujet(rs.getString("sujet"));
        r.setDescription(rs.getString("description"));
        Timestamp ts = rs.getTimestamp("date_reclamation");
        if (ts != null) r.setDateReclamation(ts.toLocalDateTime());
        r.setStatut(rs.getString("statut"));
        r.setPriorite(rs.getString("priorite"));
        r.setNomClient(rs.getString("nom_client"));
        r.setEmailClient(rs.getString("email_client"));
        r.setUserId(rs.getInt("user_id"));
        return r;
    }
    public List<Reclamation> searchByNom(String nom) {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation WHERE nom_client LIKE ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, "%" + nom + "%"); // % = contient ce mot
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Erreur searchByNom : " + e.getMessage());
        }
        return list;
    }
    // Recherche par nom + statut + priorité combinés
    public List<Reclamation> searchByNomStatutPriorite(String nom, String statut, String priorite) {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation WHERE nom_client LIKE ?";

        if (statut != null && !statut.equals("Tous"))
            sql += " AND statut = '" + statut + "'";
        if (priorite != null && !priorite.equals("Tous"))
            sql += " AND priorite = '" + priorite + "'";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, "%" + nom + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Erreur searchByNomStatutPriorite : " + e.getMessage());
        }
        return list;
    }
}