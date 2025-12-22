import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectMySQL {

    private static final String URL = "jdbc:mysql://localhost:3306/FieldInspectionConsole";
    private static final String USER = "root";
    private static final String PASSWORD = "Varsha28!";

    public static Connection getConnection() {
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect to database
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connected to FieldInspectionConsole database");
            return conn;

        } catch (ClassNotFoundException e) {
            System.out.println("❌ JDBC Driver not found! Add mysql-connector-j.jar to the project.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ Database connection failed!");
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        getConnection();
    }
}
