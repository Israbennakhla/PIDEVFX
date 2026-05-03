package services;

import interfaces.IService;
import model.Personne;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePersonne implements IService<Personne> {

    private Connection cnx ;
    public ServicePersonne(){
        this.cnx =MyDataBase.getInstance().getCnx();
    }
    @Override
    public void add(Personne p) {
        //TODO
        //1-req SQL : INSERT✅
        //2-execute req✅

        String req ="INSERT INTO `personne`(`nom`, `prenom`, `age`) VALUES (?,?,?)";

        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(3,p.getAge());
            pstm.setString(1,p.getNom());
            pstm.setString(2,p.getPrenom());

            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Personne> getAll() {
        //TODO
        // 1-req =select✅
        //2-execute✅
        //3 matching SQL <=> JAVA (Personne )✅
        //4- retourner la list personne ✅

        List<Personne> personnes = new ArrayList<>();
        String req = "SELECT * FROM `personne` ";
        try {
            Statement stm = cnx.createStatement();

            ResultSet rs = stm.executeQuery(req);
            while (rs.next()){
                Personne p = new Personne();
                p.setId(rs.getInt(1));
                p.setNom(rs.getString("nom"));
                p.setPrenom(rs.getString(3));
                p.setAge(rs.getInt("age"));


                personnes.add(p);

            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return personnes;
    }

    @Override
    public void update(Personne p) {

    }

    @Override
    public void delete(Personne p) {

    }
}
