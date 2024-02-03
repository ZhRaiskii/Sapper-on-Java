public interface StatisticsRepository {
    void sendStatistic(Statistics statistics);
    Statistics getStatistics(int userId);
}