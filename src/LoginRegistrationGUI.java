import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class LoginStrategy implements AuthenticationStrategy {
    private final UserRepository userRepository;

    public LoginStrategy(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public int performAction(String login, String password) {
        return userRepository.login(login, password);
    }
}

class RegistrationStrategy implements AuthenticationStrategy {
    private final UserRepository userRepository;

    public RegistrationStrategy(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public int performAction(String login, String password) {
        int userId = userRepository.getLastUserId() + 1;
        User user = new User(userId, login, password);
        userRepository.register(user);
        return userId;
    }
}

public class LoginRegistrationGUI extends JFrame {
    private final JTextField loginTextField;
    private final JPasswordField passwordField;
    private final PostgreSQLModule postgreSQLModule;
    private int user_id;
    private final UserRepository userRepository;
    private AuthenticationStrategy authenticationStrategy;

    public LoginRegistrationGUI() {
        userRepository = PostgreSQLModule.getInstance().getUserRepository();

        setTitle("Вход / Регистрация");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 150);
        setLocationRelativeTo(null);

        postgreSQLModule = PostgreSQLModule.getInstance();

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JLabel loginLabel = new JLabel("Логин:");
        loginTextField = new JTextField();
        JLabel passwordLabel = new JLabel("Пароль:");
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("Войти");
        JButton registerButton = new JButton("Зарегистрироваться");

        panel.add(loginLabel);
        panel.add(loginTextField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);

        loginButton.addActionListener(e -> handleAuthentication(new LoginStrategy(userRepository)));
        registerButton.addActionListener(e -> handleAuthentication(new RegistrationStrategy(userRepository)));

        add(panel);
        setVisible(true);
    }

    private void handleAuthentication(AuthenticationStrategy strategy) {
        authenticationStrategy = strategy;
        user_id = authenticationStrategy.performAction(loginTextField.getText(), new String(passwordField.getPassword()));
        if (user_id > 0) {
            startApp();
        }
    }

    private void startApp() {
        MainGUI app = new MainGUI(user_id);
        app.setVisible(true);
    }
}
