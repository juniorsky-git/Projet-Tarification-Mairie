import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestConnection {
    public static void main(String[] args) {
        String[] urls = {
            "jdbc:postgresql://shinkansen.proxy.rlwy.net:29409/railway?sslmode=disable",
            "jdbc:postgresql://shinkansen.proxy.rlwy.net:29409/railway?sslmode=require",
            "jdbc:postgresql://shinkansen.proxy.rlwy.net:29409/railway?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory",
            "jdbc:postgresql://shinkansen.proxy.rlwy.net:29409/railway"
        };
        String user = "postgres";
        String password = "cokmUcEbVEhpwgyxzUEHUGdKfnJpRRTp";

        for (String url : urls) {
            System.out.println("\n-------------------------------------------");
            System.out.println("Trying to connect to " + url);
            try {
                Class.forName("org.postgresql.Driver");
                Connection conn = DriverManager.getConnection(url, user, password);
                System.out.println("SUCCESSFULLY CONNECTED to " + url);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT 1;");
                if (rs.next()) {
                    System.out.println("Result of SELECT 1: " + rs.getInt(1));
                }
                conn.close();
                break; // stop on first success
            } catch (Exception e) {
                System.out.println("CONNECTION FAILED for " + url + ":");
                System.out.println(e.getMessage());
                if (e.getCause() != null) {
                    System.out.println("Cause: " + e.getCause().toString());
                }
            }
        }
    }
}
