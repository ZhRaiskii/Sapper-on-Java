import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginRegistrationGUI extends JFrame {

    private final JTextField loginTextField;
    private final JPasswordField passwordField;
    private final PostgreSQLModule postgreSQLModule;
    private int user_id;

    public LoginRegistrationGUI() {
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

        loginButton.addActionListener(e -> handleLogin());

        registerButton.addActionListener(e -> handleRegistration());

        add(panel);
        setVisible(true);
    }

    private void handleLogin() {
        user_id = postgreSQLModule.getLastUserId();
        String login = loginTextField.getText();
        String password = new String(passwordField.getPassword());

        if(postgreSQLModule.login(login, password) > 0){
            startApp();
        }
    }

    private void handleRegistration() {
        user_id = postgreSQLModule.getLastUserId() + 1;
        String login = loginTextField.getText();
        String password = new String(passwordField.getPassword());

        postgreSQLModule.register(user_id, login, password);

        startApp();
    }

    private void startApp(){
        MainGUI app = new MainGUI(user_id);
        app.setVisible(true);
    }
}
