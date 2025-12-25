import java.sql.*;
import java.util.Scanner;

public class FieldInspectionApp {

    public static void main(String[] args) {
        try (Connection conn = ConnectMySQL.getConnection()) {
            if (conn == null) return;

            System.out.println("=== GOVERNMENT FIELD INSPECTION SYSTEM ===");
            Scanner sc = new Scanner(System.in);

            while (true) {
                System.out.print("\nEnter username (or 'exit' to quit): ");
                String username = sc.nextLine();
                if (username.equalsIgnoreCase("exit")) break;

                System.out.print("Enter password: ");
                String password = sc.nextLine();

                String role = login(conn, username, password);
                if (role == null) {
                    System.out.println("Invalid login! Try again.");
                    continue;
                }

                recordLogin(conn, username, role);

                switch (role) {
                    case "admin":
                        adminMenu(conn, sc);
                        break;
                    case "official":
                        officialMenu(conn, sc);
                        break;
                    case "citizen":
                        citizenMenu(conn, sc, username);
                        break;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static String login(Connection conn, String username, String password) throws SQLException {
        String sql = "SELECT role FROM users WHERE username=? AND password=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getString("role");
        return null;
    }

    static void recordLogin(Connection conn, String username, String role) throws SQLException {
        String sql = "INSERT INTO login_history (username, role) VALUES (?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        ps.setString(2, role);
        ps.executeUpdate();
        System.out.println("Login recorded in database.");
    }

    static void adminMenu(Connection conn, Scanner sc) throws SQLException {
        while (true) {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. Create Official");
            System.out.println("2. Create Citizen");
            System.out.println("3. View Login History");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");
            int ch = Integer.parseInt(sc.nextLine());
            if (ch == 4) break;

            switch (ch) {
                case 1: createUser(conn, sc, "official"); break;
                case 2: createUser(conn, sc, "citizen"); break;
                case 3: viewLoginHistory(conn); break;
                default: System.out.println("Invalid choice!");
            }
        }
    }

    static void createUser(Connection conn, Scanner sc, String role) throws SQLException {
        System.out.print("Enter username: ");
        String uname = sc.nextLine();
        System.out.print("Enter password: ");
        String pass = sc.nextLine();

        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, uname);
        ps.setString(2, pass);
        ps.setString(3, role);
        ps.executeUpdate();
        System.out.println(role + " created successfully!");
    }

    static void viewLoginHistory(Connection conn) throws SQLException {
        String sql = "SELECT * FROM login_history ORDER BY id DESC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        System.out.printf("%-5s %-15s %-10s %-20s%n", "ID", "Username", "Role", "Login Time");
        System.out.println("--------------------------------------------------");
        while (rs.next()) {
            System.out.printf("%-5d %-15s %-10s %-20s%n",
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("role"),
                    rs.getTimestamp("login_time"));
        }
    }

    static void citizenMenu(Connection conn, Scanner sc, String username) throws SQLException {
        while (true) {
            System.out.println("\n--- CITIZEN MENU ---");
            System.out.println("1. Raise Complaint");
            System.out.println("2. View Pending Complaints");
            System.out.println("3. View Resolved Complaints");
            System.out.println("4. Exit");
            System.out.print("Choice: ");
            int ch = Integer.parseInt(sc.nextLine());
            if (ch == 4) break;

            switch (ch) {
                case 1: raiseComplaint(conn, sc, username); break;
                case 2: viewComplaints(conn, "Pending"); break;
                case 3: viewComplaints(conn, "Resolved"); break;
                default: System.out.println("Invalid choice!");
            }
        }
    }

    static void raiseComplaint(Connection conn, Scanner sc, String username) throws SQLException {
        System.out.print("District: "); String district = sc.nextLine();
        System.out.print("Location: "); String location = sc.nextLine();
        System.out.print("Problem: "); String desc = sc.nextLine();

        String sql = "INSERT INTO complaints (citizen_username, district, location, problem_description) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        ps.setString(2, district);
        ps.setString(3, location);
        ps.setString(4, desc);
        ps.executeUpdate();
        System.out.println("Complaint registered successfully!");
    }

    static void viewComplaints(Connection conn, String status) throws SQLException {
        String sql = "SELECT * FROM complaints WHERE status=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, status);
        ResultSet rs = ps.executeQuery();

        System.out.printf("%-5s %-15s %-12s %-15s %-30s%n", "ID", "Citizen", "District", "Location", "Problem");
        System.out.println("---------------------------------------------------------------");
        boolean hasData = false;
        while (rs.next()) {
            hasData = true;
            System.out.printf("%-5d %-15s %-12s %-15s %-30s%n",
                    rs.getInt("id"),
                    rs.getString("citizen_username"),
                    rs.getString("district"),
                    rs.getString("location"),
                    rs.getString("problem_description"));
        }
        if (!hasData) System.out.println("--- No complaints found ---");
    }

    static void officialMenu(Connection conn, Scanner sc) throws SQLException {
        while (true) {
            System.out.println("\n--- OFFICIAL MENU ---");
            System.out.println("1. View Pending Complaints");
            System.out.println("2. Mark Complaint as Resolved");
            System.out.println("3. Exit");
            System.out.print("Choice: ");
            int ch = Integer.parseInt(sc.nextLine());
            if (ch == 3) break;

            switch (ch) {
                case 1: viewComplaints(conn, "Pending"); break;
                case 2: markResolved(conn, sc); break;
                default: System.out.println("Invalid choice!");
            }
        }
    }

    static void markResolved(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Enter Complaint ID to resolve: ");
        int id = Integer.parseInt(sc.nextLine());
        String sql = "UPDATE complaints SET status='Resolved' WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Complaint marked as Resolved!");
    }
}

