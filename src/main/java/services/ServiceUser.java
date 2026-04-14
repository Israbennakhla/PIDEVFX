package services;

import model.User;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUser {

    private final Connection cnx;

    public ServiceUser() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    /**
     * Fetches all active (non-deleted) users from the `user` table.
     * Only the columns needed for the ComboBox are selected.
     */
    public List<User> getAll() {
        List<User> list = new ArrayList<>();
        String req = "SELECT id, nom, prenom, email FROM utilisateurs WHERE deleted_at IS NULL ORDER BY nom, prenom";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                list.add(new User(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            System.out.println("ServiceUser.getAll() error: " + e.getMessage());
        }
        return list;
    }
}
