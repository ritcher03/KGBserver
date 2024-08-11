package server.exem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SQLSecurity {
    private static Connection connection;

    // Initialize database connection
    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Replace with your database URL, username, and password
                connection = DriverManager.getConnection("jdbc:mysql://192.168.0.173:3306/chat_app", "novyuzivatel", "Mentoss97@");
            } catch (SQLException e) {
                e.printStackTrace();
                connection = null;
            }
        }
        return connection;
    }

    public static boolean isDatabaseConnected() {
        return getConnection() != null;
    }

    // Authenticate user with provided username and password
    public static boolean authenticateUser(String clientName, String password) {
        boolean isAuthenticated = false;
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, clientName);
            pstmt.setString(2, password); // Ensure passwords are stored securely and hashed in real-world scenarios

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    isAuthenticated = true; // User found in the database
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isAuthenticated;
    }

    public static void notifyDatabaseStatus(List<ClientInfo> clients) {
        boolean isConnected = isDatabaseConnected();
        String statusMessage = isConnected ? "Stav databáze: Připojeno" : "Stav databáze: Nepřipojeno";

        // Notify all clients about the database status
        synchronized (clients) {
            for (ClientInfo client : clients) {
                client.getHandler().out.println(statusMessage);
            }
        }
    }
}
