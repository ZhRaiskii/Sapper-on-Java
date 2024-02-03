import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String DB_URL = "jdbc:postgresql://127.0.0.1:5432/Sapper";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "lolhilol";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
