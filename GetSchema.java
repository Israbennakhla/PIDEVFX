import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class GetSchema {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/sitmypet";
        String user = "root";
        String password = "";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM utilisateurs LIMIT 1")) {
             
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            
            System.out.println("Columns in table 'utilisateurs':");
            for (int i = 1; i <= columnCount; i++) {
                System.out.println(rsmd.getColumnName(i) + " (" + rsmd.getColumnTypeName(i) + ")");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
