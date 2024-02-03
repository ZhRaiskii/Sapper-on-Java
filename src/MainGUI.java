import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Objects;

public class MainGUI extends JFrame implements GameObserver {

    private MinesweeperViewModel viewModel;
    private JButton[][] buttons;
    private JPanel topPanel;
    private JPanel gamePanel;
    private boolean isFlagMode = false;
    private boolean isPlaying = false;
    private JLabel timeLabel;
    private JLabel flagsLabel;
    private int countFlags;
    private int elapsedTime;
    private Timer timer;
    private final int user_id;
    private final StatisticsRepository statisticsRepository;

    public MainGUI(int user_id) {
        this.user_id = user_id;
        initializeViewModel(user_id);
        initializeUI();
        createMenu();
        createTopPanel();
        addGameObserver(this);
        statisticsRepository = PostgreSQLModule.getInstance().getStatisticsRepository();
    }

    private void initializeViewModel(int user_id) {
        viewModel = new MinesweeperViewModel(user_id);
        viewModel.addObserver(this);
    }

    @Override
    public void gameWon() {
        showWinMessage();
        int elapsedTime = extractElapsedTime();
        viewModel.updateStatistics(elapsedTime);
        endGame();
    }

    private void showWinMessage() {
        JOptionPane.showMessageDialog(this, "Вы выиграли!");
    }

    private int extractElapsedTime() {
        return Integer.parseInt(timeLabel.getText().replaceAll("\\D", ""));
    }

    private void updateStatistics(int elapsedTime) {
        viewModel.updateStatistics(elapsedTime);
    }

    private void updateUIAfterGame() {
        openAllCells();
        disableAllButtons();
        stopTimer();
    }

    private void updateUIAfterCellClick(int row, int column) {
        if (isFlagMode) {
            updateUIConcrete(row, column);
        } else {
            buttons[row][column].setText(getEmoji(viewModel.getValueAt(row, column)));
        }
    }

    private void updateUIAfterGameWon() {
        updateUIAfterGame();
        notifyGameObservers();
    }

    private void initializeUI() {
        setTitle("Сапёр");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        topPanel = new JPanel();
        add(topPanel, BorderLayout.NORTH);

        gamePanel = new JPanel(new GridLayout(viewModel.getBoardSize(), viewModel.getBoardSize()));
        add(gamePanel, BorderLayout.CENTER);

        updateGameBoard();
        disableAllButtons();
        setSize(400, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createTopPanel() {
        JButton startButton = new JButton("Старт");
        startButton.addActionListener(e -> startGame());

        JButton toggleModeButton = getToggleModeButton();
        timeLabel = new JLabel("Время: 0");
        flagsLabel = new JLabel("Флаги: " + countFlags);

        topPanel.add(startButton);
        topPanel.add(toggleModeButton);
        topPanel.add(timeLabel);
        topPanel.add(flagsLabel);
    }

    private JButton getToggleModeButton() {
        JButton toggleModeButton = new JButton("⛏️");
        toggleModeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Objects.equals(toggleModeButton.getText(), "⛏️")) {
                    toggleModeButton.setText("\uD83D\uDEA9");
                    isFlagMode = true;
                } else {
                    toggleModeButton.setText("⛏️");
                    isFlagMode = false;
                }
            }
        });
        return toggleModeButton;
    }

    private void updateGameBoard() {
        Font emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 14);
        gamePanel.setLayout(new GridLayout(viewModel.getBoardSize(), viewModel.getBoardSize()));
        buttons = new JButton[viewModel.getBoardSize()][viewModel.getBoardSize()];
        gamePanel.removeAll();
        for (int i = 0; i < viewModel.getBoardSize(); i++) {
            for (int j = 0; j < viewModel.getBoardSize(); j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].setFont(emojiFont);
                buttons[i][j].setPreferredSize(new Dimension(30, 30));
                buttons[i][j].addActionListener(new ButtonClickListener(i, j));
                gamePanel.add(buttons[i][j]);
            }
        }
        gamePanel.repaint();
        gamePanel.revalidate();
        disableAllButtons();
    }

    private void startGame() {
        isPlaying = true;
        updateGameBoard();
        activateAllButtons();
        viewModel.startNewGame(viewModel.getBoardSize(), viewModel.getMinesCount());
        countFlags = viewModel.getMinesCount();
        flagsLabel.setText("Флаги:" + countFlags);
        startTimer();
    }

    private void endGame() {
        isPlaying = false;
        updateUIAfterGame();
    }

    private JMenu createChooseSizeSubMenu() {
        JMenu chooseSizeSubMenu = new JMenu("Выбрать размер поля");

        JMenuItem size5x5Item = createSizeMenuItem("5x5", 5, 400);
        JMenuItem size8x8Item = createSizeMenuItem("8x8", 8, 700);
        JMenuItem size10x10Item = createSizeMenuItem("16x16", 16, 1000);

        chooseSizeSubMenu.add(size5x5Item);
        chooseSizeSubMenu.add(size8x8Item);
        chooseSizeSubMenu.add(size10x10Item);

        return chooseSizeSubMenu;
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu settingsMenu = new JMenu("Настройки");

        JMenu chooseSizeSubMenu = createChooseSizeSubMenu();

        settingsMenu.add(chooseSizeSubMenu);
        menuBar.add(settingsMenu);

        JMenuItem statisticsMenuItem = new JMenuItem("Статистика");
        statisticsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStatisticsWindow();
            }
        });
        menuBar.add(statisticsMenuItem);

        setJMenuBar(menuBar);
    }

    private void showStatisticsWindow() {
        JFrame statisticsFrame = new JFrame("Статистика");
        statisticsFrame.setLayout(new GridLayout(5, 2, 5, 5)); // 5 rows, 2 columns, with gaps
        Statistics stats = statisticsRepository.getStatistics(user_id);
        int statistic_5x5 = 0;
        int statistic_8x8 = 0;
        int statistic_16x16 = 0;
        if (stats != null) {
            statistic_5x5 = stats.getStatistic_5x5();
            statistic_8x8 = stats.getStatistic_8x8();
            statistic_16x16 = stats.getStatistic_16x16();
        }
        if (viewModel.getBoardSize() == 5 && statistic_5x5 > elapsedTime) {
            statistic_5x5 = elapsedTime;
        } else if (viewModel.getBoardSize() == 8 && statistic_8x8 > elapsedTime) {
            statistic_8x8 = elapsedTime;
        } else if (viewModel.getBoardSize() == 16 && statistic_16x16 > elapsedTime) {
            statistic_16x16 = elapsedTime;
        }
        JLabel nameLabel = new JLabel("Ваше имя:");
        JTextField nameTextField = new JTextField();
        nameTextField.setEditable(false);
        statisticsFrame.add(nameLabel);
        statisticsFrame.add(nameTextField);

        JLabel record5x5Label = new JLabel("Рекорд по времени в режиме 5x5:");
        JTextField record5x5TextField = new JTextField();
        record5x5TextField.setEditable(false);
        record5x5TextField.setText(String.valueOf(statistic_5x5));
        statisticsFrame.add(record5x5Label);
        statisticsFrame.add(record5x5TextField);

        JLabel record8x8Label = new JLabel("Рекорд по времени в режиме 8x8:");
        JTextField record8x8TextField = new JTextField();
        record8x8TextField.setEditable(false);
        record8x8TextField.setText(String.valueOf(statistic_8x8));
        statisticsFrame.add(record8x8Label);
        statisticsFrame.add(record8x8TextField);

        JLabel record16x16Label = new JLabel("Рекорд по времени в режиме 16x16:");
        JTextField record16x16TextField = new JTextField();
        record16x16TextField.setEditable(false);
        record16x16TextField.setText(String.valueOf(statistic_16x16));
        statisticsFrame.add(record16x16Label);
        statisticsFrame.add(record16x16TextField);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                statisticsFrame.dispose();
            }
        });
        statisticsFrame.add(okButton);

        statisticsFrame.setSize(600, 250);
        statisticsFrame.setLocationRelativeTo(this);
        statisticsFrame.setVisible(true);
    }

    private JMenuItem createSizeMenuItem(String label, int size, int windowSize) {
        JMenuItem sizeItem = new JMenuItem(label);
        sizeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewModel.startNewGame(size, size * 2); // Assuming mines count is twice the board size
                updateGameBoard();
                setBounds(0, 0, windowSize, windowSize);
            }
        });
        return sizeItem;
    }

    private class ButtonClickListener implements ActionListener {
        private final int row;
        private final int col;

        public ButtonClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (isPlaying) {
                if (!isFlagMode) {
                    if (!viewModel.getFlag(row, col)) {
                        if (viewModel.getValueAt(row, col) == 0) {
                            viewModel.openCell(row, col);
                            updateUI();
                        } else if (viewModel.getValueAt(row, col) == -1) {
                            endGame();
                        } else if (viewModel.getValueAt(row, col) != -2) {
                            updateUIConcrete(row, col);
                        }
                    }
                } else {
                    updateUIAfterCellClick(row, col);
                }
                checkGameWon();
            }
        }

        private void checkGameWon() {
            if (viewModel.isGameWon()) {
                updateUIAfterGameWon();
            }
        }
    }

    private void openAllCells() {
        for (int i = 0; i < viewModel.getBoardSize(); i++) {
            for (int j = 0; j < viewModel.getBoardSize(); j++) {
                buttons[i][j].setText(getEmoji(viewModel.getValueAt(i, j)));
            }
        }
    }

    private void disableAllButtons() {
        for (int i = 0; i < viewModel.getBoardSize(); i++) {
            for (int j = 0; j < viewModel.getBoardSize(); j++) {
                buttons[i][j].setEnabled(false);
            }
        }
    }

    private void activateAllButtons() {
        for (int i = 0; i < viewModel.getBoardSize(); i++) {
            for (int j = 0; j < viewModel.getBoardSize(); j++) {
                buttons[i][j].setEnabled(true);
            }
        }
    }

    private void updateUIConcrete(int row, int column) {
        if (isFlagMode) {
            if (Objects.equals(buttons[row][column].getText(), "\uD83D\uDEA9")) {
                buttons[row][column].setText("");
                viewModel.removeFlag(row, column);
                countFlags++;
                updateFlagsUI();
            } else if (countFlags > 0) {
                buttons[row][column].setText("\uD83D\uDEA9");
                viewModel.setFlag(row, column);
                countFlags--;
                updateFlagsUI();
            }
        } else {
            buttons[row][column].setText(getEmoji(viewModel.getValueAt(row, column)));
        }
    }

    private void updateUI() {
        for (int i = 0; i < viewModel.getBoardSize(); i++) {
            for (int j = 0; j < viewModel.getBoardSize(); j++) {
                int value = viewModel.getValueAt(i, j);

                if (value == -2) {
                    buttons[i][j].setBackground(Color.GRAY);
                    buttons[i][j].setEnabled(false);

                    if (i + 1 < viewModel.getBoardSize() && viewModel.getValueAt(i + 1, j) >= 1 && viewModel.getValueAt(i + 1, j) < 4) {
                        buttons[i + 1][j].setText(getEmoji(viewModel.getValueAt(i + 1, j)));
                    }
                    if (i - 1 >= 0 && viewModel.getValueAt(i - 1, j) >= 1 && viewModel.getValueAt(i - 1, j) < 4) {
                        buttons[i - 1][j].setText(getEmoji(viewModel.getValueAt(i - 1, j)));
                    }
                    if (j + 1 < viewModel.getBoardSize() && viewModel.getValueAt(i, j + 1) >= 1 && viewModel.getValueAt(i, j + 1) < 4) {
                        buttons[i][j + 1].setText(getEmoji(viewModel.getValueAt(i, j + 1)));
                    }
                    if (j - 1 >= 0 && viewModel.getValueAt(i, j - 1) >= 1 && viewModel.getValueAt(i, j - 1) < 4) {
                        buttons[i][j - 1].setText(getEmoji(viewModel.getValueAt(i, j - 1)));
                    }
                }
            }
        }
    }

    private String getEmoji(int value) {
        return switch (value) {
            case -1 -> ("💣");
            case 1 -> ("1️⃣");
            case 2 -> ("2️⃣");
            case 3 -> ("3️⃣");
            case 4 -> ("4️⃣");
            default -> "";
        };
    }

    private void addGameObserver(GameObserver observer) {
        viewModel.addObserver((MainGUI) observer);
    }

    private void notifyGameObservers() {
        viewModel.notifyGameObservers();
    }

    private void startTimer() {
        elapsedTime = 0;
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                elapsedTime++;
                updateElapsedTime();
            }
        });
        timer.start();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    private void updateElapsedTime() {
        timeLabel.setText("Время: " + elapsedTime);
    }

    private void updateFlagsUI() {
        flagsLabel.setText("Флаги: " + countFlags);
    }
    public class StatisticsUpdater {
        private final PostgreSQLModule postgreSQLModule;

        public StatisticsUpdater(PostgreSQLModule postgreSQLModule) {
            this.postgreSQLModule = postgreSQLModule;
        }

        public void updateStatistics(int user_id, MinesweeperGame minesweeperGame, int elapsedTime) {
            Statistics stats = statisticsRepository.getStatistics(user_id);

            if (stats != null) {
                int boardSize = minesweeperGame.getBoardSize();
                int currentStatistic = switch (boardSize) {
                    case 5 -> stats.getStatistic_5x5();
                    case 8 -> stats.getStatistic_8x8();
                    case 16 -> stats.getStatistic_16x16();
                    default -> 0;
                };

                if (currentStatistic > elapsedTime || currentStatistic == 0) {
                    switch (boardSize) {
                        case 5 -> stats.setStatistic_5x5(elapsedTime);
                        case 8 -> stats.setStatistic_8x8(elapsedTime);
                        case 16 -> stats.setStatistic_16x16(elapsedTime);
                    }

                    statisticsRepository.sendStatistic(stats);
                }
            }
        }
    }
    public class MinesweeperViewModel {
        private MinesweeperGame minesweeperGame;
        private final ArrayList<GameObserver> observers = new ArrayList<>();
        private final int user_id;
        private final PostgreSQLModule postgreSQLModule;

        public MinesweeperViewModel(int user_id) {
            this.user_id = user_id;
            initializeMinesweeperGame();
            postgreSQLModule = PostgreSQLModule.getInstance();
        }

        public void updateStatistics(int elapsedTime) {
            StatisticsUpdater statisticsUpdater = new StatisticsUpdater(postgreSQLModule);
            statisticsUpdater.updateStatistics(user_id, minesweeperGame, elapsedTime);
        }

        private void initializeMinesweeperGame() {
            this.minesweeperGame = MinesweeperGame.getInstance(5, 2);
        }

        public void addObserver(MainGUI observer) {
            observers.add(observer);
        }

        public void notifyGameObservers() {
            for (GameObserver observer : observers) {
                observer.gameWon();
            }
        }

        public void startNewGame(int boardSize, int minesCount) {
            minesweeperGame = MinesweeperGame.getInstance(boardSize, minesCount);
        }

        public int getBoardSize() {
            return minesweeperGame.getBoardSize();
        }

        public int getMinesCount() {
            return minesweeperGame.getMinesCount();
        }

        public int getValueAt(int row, int col) {
            return minesweeperGame.getValueAt(row, col);
        }

        public void openCell(int row, int col) {
            minesweeperGame.openCell(row, col);
        }

        public boolean getFlag(int row, int col) {
            return minesweeperGame.getFlag(row, col);
        }

        public void setFlag(int row, int col) {
            minesweeperGame.setFlag(row, col);
        }

        public void removeFlag(int row, int col) {
            minesweeperGame.removeFlag(row, col);
        }

        public boolean isGameWon() {
            return minesweeperGame.isGameWon();
        }
    }
}
