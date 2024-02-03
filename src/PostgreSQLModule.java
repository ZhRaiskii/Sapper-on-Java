public class PostgreSQLModule {
    private static PostgreSQLModule instance;
    private final UserRepository userRepository;
    private final StatisticsRepository statisticsRepository;

    private PostgreSQLModule() {
        DatabaseConnector databaseConnector = new DatabaseConnector();
        this.userRepository = new UserRepositoryImpl(databaseConnector);
        this.statisticsRepository = new StatisticsRepositoryImpl(databaseConnector);
    }

    public static PostgreSQLModule getInstance() {
        if (instance == null) {
            instance = new PostgreSQLModule();
        }
        return instance;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public StatisticsRepository getStatisticsRepository() {
        return statisticsRepository;
    }
}
