package com.sitmypet.services;

import com.sitmypet.interfaces.IService;
import com.sitmypet.model.Pet;
import com.sitmypet.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePet implements IService<Pet> {

    private Connection cnx;

    public ServicePet() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    // ── Ajouter ───────────────────────────────────────────────
    @Override
    public void add(Pet p) {
        String req = "INSERT INTO pet (name, birth_date, type_pet, breed, weight, description, gender, " +
                "has_contagious_disease, has_medical_record, has_critical_condition, is_vaccinated, image_name) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, p.getName());
            pstm.setDate(2, p.getBirthDate());
            pstm.setString(3, p.getTypePet());
            pstm.setString(4, p.getBreed());
            pstm.setFloat(5, p.getWeight());
            pstm.setString(6, p.getDescription());
            pstm.setString(7, p.getGender());
            pstm.setBoolean(8, p.isHasContagiousDisease());
            pstm.setBoolean(9, p.isHasMedicalRecord());
            pstm.setBoolean(10, p.isHasCriticalCondition());
            pstm.setBoolean(11, p.isVaccinated());
            pstm.setString(12, p.getImageName());
            pstm.executeUpdate();
            System.out.println("✅ Animal ajouté : " + p.getName());
        } catch (SQLException e) {
            System.out.println("❌ Erreur add : " + e.getMessage());
        }
    }

    // ── Lire tous ─────────────────────────────────────────────
    @Override
    public List<Pet> getAll() {
        List<Pet> pets = new ArrayList<>();
        String req = "SELECT * FROM pet";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                Pet p = new Pet();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setBirthDate(rs.getDate("birth_date"));
                p.setTypePet(rs.getString("type_pet"));
                p.setBreed(rs.getString("breed"));
                p.setWeight(rs.getFloat("weight"));
                p.setDescription(rs.getString("description"));
                p.setGender(rs.getString("gender"));
                p.setHasContagiousDisease(rs.getBoolean("has_contagious_disease"));
                p.setHasMedicalRecord(rs.getBoolean("has_medical_record"));
                p.setHasCriticalCondition(rs.getBoolean("has_critical_condition"));
                p.setVaccinated(rs.getBoolean("is_vaccinated"));
                p.setImageName(rs.getString("image_name"));
                pets.add(p);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur getAll : " + e.getMessage());
        }
        return pets;
    }

    // ── Modifier ✅ ───────────────────────────────────────────
    @Override
    public void update(Pet p) {
        String req = "UPDATE pet SET name=?, birth_date=?, type_pet=?, breed=?, weight=?, " +
                "description=?, gender=?, has_contagious_disease=?, has_medical_record=?, " +
                "has_critical_condition=?, is_vaccinated=?, image_name=? WHERE id=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, p.getName());
            pstm.setDate(2, p.getBirthDate());
            pstm.setString(3, p.getTypePet());
            pstm.setString(4, p.getBreed());
            pstm.setFloat(5, p.getWeight());
            pstm.setString(6, p.getDescription());
            pstm.setString(7, p.getGender());
            pstm.setBoolean(8, p.isHasContagiousDisease());
            pstm.setBoolean(9, p.isHasMedicalRecord());
            pstm.setBoolean(10, p.isHasCriticalCondition());
            pstm.setBoolean(11, p.isVaccinated());
            pstm.setString(12, p.getImageName());
            pstm.setInt(13, p.getId());   // ✅ WHERE id = ?

            int rows = pstm.executeUpdate();
            System.out.println("✅ Animal modifié (id=" + p.getId() + ") — lignes affectées : " + rows);
        } catch (SQLException e) {
            System.out.println("❌ Erreur update : " + e.getMessage());
        }
    }

    // ── Supprimer ✅ ──────────────────────────────────────────
    @Override
    public void delete(Pet p) {
        String req = "DELETE FROM pet WHERE id=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, p.getId());   // ✅ WHERE id = ?

            int rows = pstm.executeUpdate();
            System.out.println("✅ Animal supprimé (id=" + p.getId() + ") — lignes affectées : " + rows);
        } catch (SQLException e) {
            System.out.println("❌ Erreur delete : " + e.getMessage());
        }
    }
}