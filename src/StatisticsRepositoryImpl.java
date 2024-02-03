import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StatisticsRepositoryImpl implements StatisticsRepository {
    private final DatabaseConnector databaseConnector;

    public StatisticsRepositoryImpl(DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
    }

    @Override
    public void sendStatistic(Statistics stats) {
        try (Connection connection = databaseConnector.getConnection()) {
            String updateQuery = "UPDATE Statistics SET statistic_5x5 = ?, statistic_8x8 = ?, statistic_16x16 = ? WHERE user_id = ?";
            try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                updateStatement.setInt(1, stats.getStatistic_5x5());
                updateStatement.setInt(2, stats.getStatistic_8x8());
                updateStatement.setInt(3, stats.getStatistic_16x16());
                updateStatement.setInt(4, stats.getUser_id());

                int rowsUpdated = updateStatement.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("Statistic updated successfully.");
                } else {
                    System.out.println("Failed to update statistic.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Statistics getStatistics(int userId) {
        Statistics statistics = null;

        try (Connection connection = databaseConnector.getConnection()) {
            String sqlQuery = "SELECT * FROM Statistics WHERE user_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setInt(1, userId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int statistic_5x5 = resultSet.getInt("statistic_5x5");
                        int statistic_8x8 = resultSet.getInt("statistic_8x8");
                        int statistic_16x16 = resultSet.getInt("statistic_16x16");

                        statistics = new Statistics(userId, statistic_5x5, statistic_8x8, statistic_16x16);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return statistics;
    }
}