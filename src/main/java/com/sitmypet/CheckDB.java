package com.sitmypet;

import com.sitmypet.utils.MyDatabase;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckDB {
    public static void main(String[] args) {
        try {
            Connection cnx = MyDatabase.getInstance().getConnection();
            
            System.out.println("--- USERS ---");
            try (Statement st = cnx.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id, email FROM utilisateurs")) {
                while (rs.next()) {
                    System.out.println("User ID: " + rs.getInt("id") + " - " + rs.getString("email"));
                }
            }

            System.out.println("\n--- PETS ---");
            try (Statement st = cnx.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id, name, owner_id FROM pet")) {
                while (rs.next()) {
                    System.out.println("Pet ID: " + rs.getInt("id") + " - " + rs.getString("name") + " (owner: " + rs.getInt("owner_id") + ")");
                }
            }
            
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
