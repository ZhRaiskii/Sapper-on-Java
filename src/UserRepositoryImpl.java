import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepositoryImpl implements UserRepository {
    private final DatabaseConnector databaseConnector;

    public UserRepositoryImpl(DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
    }

    @Override
    public int login(String login, String password) {
        int userId = 0;

        try (Connection connection = databaseConnector.getConnection()) {
            String sqlQuery = "SELECT user_id FROM Users WHERE login = ? AND password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setString(1, login);
                preparedStatement.setString(2, password);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        userId = resultSet.getInt("user_id");
                    } else {
                        System.out.println("User not found");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return userId;
    }

    @Override
    public void register(User user) {
        try (Connection connection = databaseConnector.getConnection()) {
            String sqlQuery = "INSERT INTO Users (user_id, login, password) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setInt(1, user.getUserId());
                preparedStatement.setString(2, user.getLogin());
                preparedStatement.setString(3, user.getPassword());

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

    @Override
    public User getById(int userId) {
        User user = null;

        try (Connection connection = databaseConnector.getConnection()) {
            String sqlQuery = "SELECT * FROM Users WHERE user_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setInt(1, userId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String login = resultSet.getString("login");
                        String password = resultSet.getString("password");

                        user = new User(userId, login, password);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    @Override
    public int getLastUserId() {
        int lastUserId = 0;

        try (Connection connection = databaseConnector.getConnection()) {
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