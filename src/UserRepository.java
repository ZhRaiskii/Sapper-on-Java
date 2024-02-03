public interface UserRepository {
    int login(String login, String password);
    void register(User user);
    User getById(int userId);
    int getLastUserId();
}