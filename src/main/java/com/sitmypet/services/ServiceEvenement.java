package com.sitmypet.services;

import com.sitmypet.services.IService;
import com.sitmypet.model.Evenement;
import com.sitmypet.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceEvenement {
    private Connection cnx;
    public ServiceEvenement() {
        this.cnx = MyDatabase.getInstance().getConnection();
    }

    public void add(Evenement e) {
        String req = "INSERT INTO `event`(`name`, `date`, `heure`, `addresse`, `description`) VALUES (?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, e.getName());
            pstm.setDate(2, e.getDate());
            pstm.setString(3, e.getHeure());
            pstm.setString(4, e.getAddresse());
            pstm.setString(5, e.getDescription());
            pstm.executeUpdate();
            System.out.println("Événement ajouté avec succès !");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public List<Evenement> getAll() {
        List<Evenement> evenements = new ArrayList<>();
        String req = "SELECT * FROM `event`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                Evenement e = new Evenement();
                e.setId(rs.getInt("id"));
                e.setName(rs.getString("name"));
                e.setDate(rs.getDate("date"));
                e.setHeure(rs.getString("heure"));
                e.setAddresse(rs.getString("addresse"));
                e.setDescription(rs.getString("description"));
                evenements.add(e);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return evenements;
    }

    public void update(Evenement e) {
        String req = "UPDATE `event` SET `name`=?, `date`=?, `heure`=?, `addresse`=?, `description`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, e.getName());
            pstm.setDate(2, e.getDate());
            pstm.setString(3, e.getHeure());
            pstm.setString(4, e.getAddresse());
            pstm.setString(5, e.getDescription());
            pstm.setInt(6, e.getId());
            pstm.executeUpdate();
            System.out.println("Événement modifié avec succès !");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void delete(Evenement e) {
        String req = "DELETE FROM `event` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, e.getId());
            pstm.executeUpdate();
            System.out.println("Événement supprimé avec succès !");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
