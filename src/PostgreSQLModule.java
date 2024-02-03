import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class PostgreSQLModule {
    private static PostgreSQLModule instance;
    private static final String DB_URL = "jdbc:postgresql://127.0.0.1:5432/Sapper";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "lolhilol";

    private PostgreSQLModule() {
    }

    public static PostgreSQLModule getInstance() {
        if (instance == null) {
            instance = new PostgreSQLModule();
        }
        return instance;
    }

    public int login(String login, String password) {
        int user_id = 0;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sqlQuery = "SELECT user_id FROM Users WHERE login = ? AND password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setString(1, login);
                preparedStatement.setString(2, password);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        user_id = resultSet.getInt("user_id");
                    } else {
                        System.out.println("User not found");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user_id;
    }


    public void register(int user_id, String login, String password) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sqlQuery = "INSERT INTO Users (user_id, login, password) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setInt(1, user_id);
                preparedStatement.setString(2, login);
                preparedStatement.setString(3, password);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Registration successful.");
                } else {
                    System.out.println("Registration failed.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void sendStatistic(int user_id, int statistic_5x5, int statistic_8x8, int statistic_16x16) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String checkQuery = "SELECT * FROM Statistics WHERE user_id = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
                checkStatement.setInt(1, user_id);

                try (ResultSet resultSet = checkStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String updateQuery = "UPDATE Statistics SET statistic_5x5 = ?, statistic_8x8 = ?, statistic_16x16 = ? WHERE user_id = ?";
                        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                            updateStatement.setInt(1, statistic_5x5);
                            updateStatement.setInt(2, statistic_8x8);
                            updateStatement.setInt(3, statistic_16x16);
                            updateStatement.setInt(4, user_id);

                            int rowsUpdated = updateStatement.executeUpdate();
                            if (rowsUpdated > 0) {
                                System.out.println("Statistic updated successfully.");
                            } else {
                                System.out.println("Failed to update statistic.");
                            }
                        }
                    } else {
                        String insertQuery = "INSERT INTO Statistics (user_id, statistic_5x5, statistic_8x8, statistic_16x16) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                            insertStatement.setInt(1, user_id);
                            insertStatement.setInt(2, statistic_5x5);
                            insertStatement.setInt(3, statistic_8x8);
                            insertStatement.setInt(4, statistic_16x16);

                            int rowsInserted = insertStatement.executeUpdate();
                            if (rowsInserted > 0) {
                                System.out.println("New statistic record inserted successfully.");
                            } else {
                                System.out.println("Failed to insert new statistic record.");
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Statistics getStatistics(int user_id) {
        Statistics statistics = null;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sqlQuery = "SELECT * FROM Statistics WHERE user_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setInt(1, user_id);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int statistic_5x5 = resultSet.getInt("statistic_5x5");
                        int statistic_8x8 = resultSet.getInt("statistic_8x8");
                        int statistic_16x16 = resultSet.getInt("statistic_16x16");

                        statistics = new Statistics(user_id, statistic_5x5, statistic_8x8, statistic_16x16);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return statistics;
    }

    public int getLastUserId() {
        int lastUserId = 0;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sqlQuery = "SELECT user_id FROM Users ORDER BY user_id DESC LIMIT 1";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        lastUserId = resultSet.getInt("user_id");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lastUserId;
    }
}
