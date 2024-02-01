import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Objects;
import javax.swing.*;

interface GameObserver {
    void gameWon();
}

public class MainGUI extends JFrame implements GameObserver{

    private MinesweeperGame minesweeperGame;
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
    private ArrayList<GameObserver> observers = new ArrayList<GameObserver>();

    public MainGUI() {
        initializeMinesweeperGame();
        initializeUI();
        createMenu();
        createTopPanel();
        addGameObserver(this);
    }
    @Override
    public void gameWon() {
        JOptionPane.showMessageDialog(this, "Вы выиграли!");
        endGame();
    }

    private void initializeUI() {
        setTitle("Сапёр");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        topPanel = new JPanel();
        add(topPanel, BorderLayout.NORTH);

        gamePanel = new JPanel(new GridLayout(minesweeperGame.getBoardSize(), minesweeperGame.getBoardSize()));
        add(gamePanel, BorderLayout.CENTER);

        updateGameBoard();
        disableAllButtons();
        setSize(400, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createTopPanel() {
        JButton startButton = new JButton("Старт");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });
        JButton toggleModeButton = getToggleModeButton();
        timeLabel = new JLabel("Время: 0");
        flagsLabel = new JLabel("Флаги: " + String.valueOf(countFlags));

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
                if(toggleModeButton.getText() == "⛏️"){
                    toggleModeButton.setText("\uD83D\uDEA9");
                    isFlagMode = true;
                }
                else{
                    toggleModeButton.setText("⛏️");
                    isFlagMode = false;
                }
            }
        });
        return toggleModeButton;
    }

    private void updateGameBoard() {
        Font emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 14);
        gamePanel.setLayout(new GridLayout(minesweeperGame.getBoardSize(), minesweeperGame.getBoardSize()));
        buttons = new JButton[minesweeperGame.getBoardSize()][minesweeperGame.getBoardSize()];
        gamePanel.removeAll();  // Удаляем все кнопки из панели
        for (int i = 0; i < minesweeperGame.getBoardSize(); i++) {
            for (int j = 0; j < minesweeperGame.getBoardSize(); j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].setFont(emojiFont); // Установка шрифта для кнопок
                buttons[i][j].setPreferredSize(new Dimension(30, 30)); // Установка размера кнопок
                buttons[i][j].addActionListener(new ButtonClickListener(i, j));
                gamePanel.add(buttons[i][j]);
            }
        }
        gamePanel.repaint();  // Обновление панели игры
        gamePanel.revalidate();
        disableAllButtons();
    }

    private void startGame() {
        isPlaying = true;
        updateGameBoard();
        activateAllButtons();
        minesweeperGame.initializeBoard();
        countFlags = minesweeperGame.getMinesCount();
        flagsLabel.setText("Флаги:" + countFlags);
        startTimer();
    }

    private void endGame() {
        isPlaying = false;
        openAllCells();
        disableAllButtons();
        stopTimer();
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

        setJMenuBar(menuBar);
    }


    private JMenuItem createSizeMenuItem(String label, int size, int windowSize) {
        JMenuItem sizeItem = new JMenuItem(label);
        sizeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                minesweeperGame.setBorderSize(size);
                updateGameBoard();
                minesweeperGame.initializeBoard();
                setBounds(0,0,windowSize, windowSize);
            }
        });
        return sizeItem;
    }

    private void initializeMinesweeperGame() {
        this.minesweeperGame = new MinesweeperGame(5, 2);
    }

    private class ButtonClickListener implements ActionListener {
        private int row, col;

        public ButtonClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (isPlaying) {
                if (!isFlagMode) {
                    if (!minesweeperGame.getFlag(row, col)) {
                        if (minesweeperGame.getValueAt(row, col) == 0) {
                            minesweeperGame.openCell(row, col);
                            updateUI();
                        } else if (minesweeperGame.getValueAt(row, col) == -1) {
                            endGame();
                        } else if (minesweeperGame.getValueAt(row, col) != -2) {
                            updateUIConcrete(row, col);
                        }
                    }
                } else {
                    updateUIConcrete(row, col);
                }
                checkGameWon();
            }
        }

        private void checkGameWon() {
            if (minesweeperGame.isGameWon()) {
                notifyGameObservers();
            }
        }
    }

    private void openAllCells() {
        for (int i = 0; i < minesweeperGame.getBoardSize(); i++) {
            for (int j = 0; j < minesweeperGame.getBoardSize(); j++) {
                buttons[i][j].setText(getEmoji(minesweeperGame.getValueAt(i, j)));
            }
        }
    }

    private void disableAllButtons() {
        for (int i = 0; i < minesweeperGame.getBoardSize(); i++) {
            for (int j = 0; j < minesweeperGame.getBoardSize(); j++) {
                buttons[i][j].setEnabled(false);
            }
        }
    }
    private void activateAllButtons() {
        for (int i = 0; i < minesweeperGame.getBoardSize(); i++) {
            for (int j = 0; j < minesweeperGame.getBoardSize(); j++) {
                buttons[i][j].setEnabled(true);
            }
        }
    }

    private void updateUIConcrete(int row, int column) {
        if(isFlagMode){
            if(Objects.equals(buttons[row][column].getText(), "\uD83D\uDEA9")){
                buttons[row][column].setText("");
                minesweeperGame.removeFlag(row,column);
                countFlags++;
                updateFlagsUI();
            }
            else if(countFlags > 0){
                buttons[row][column].setText("\uD83D\uDEA9");
                minesweeperGame.setFlag(row,column);
                countFlags--;
                updateFlagsUI();
            }
        }
        else{
            buttons[row][column].setText(getEmoji(minesweeperGame.getValueAt(row, column)));
        }
    }

    private void updateUI() {
        for (int i = 0; i < minesweeperGame.getBoardSize(); i++) {
            for (int j = 0; j < minesweeperGame.getBoardSize(); j++) {
                int value = minesweeperGame.getValueAt(i, j);

                if (value == -2) {
                    buttons[i][j].setBackground(Color.GRAY);
                    buttons[i][j].setEnabled(false);

                    if (i + 1 < minesweeperGame.getBoardSize() && minesweeperGame.getValueAt(i + 1, j) >= 1 && minesweeperGame.getValueAt(i + 1, j) < 4) {
                        buttons[i + 1][j].setText(getEmoji(minesweeperGame.getValueAt(i + 1, j)));
                    }
                    if (i - 1 >= 0 && minesweeperGame.getValueAt(i - 1, j) >= 1 && minesweeperGame.getValueAt(i - 1, j) < 4) {
                        buttons[i - 1][j].setText(getEmoji(minesweeperGame.getValueAt(i - 1, j)));
                    }
                    if (j + 1 < minesweeperGame.getBoardSize() && minesweeperGame.getValueAt(i, j + 1) >= 1 && minesweeperGame.getValueAt(i, j + 1) < 4) {
                        buttons[i][j + 1].setText(getEmoji(minesweeperGame.getValueAt(i, j + 1)));
                    }
                    if (j - 1 >= 0 && minesweeperGame.getValueAt(i, j - 1) >= 1 && minesweeperGame.getValueAt(i, j - 1) < 4) {
                        buttons[i][j - 1].setText(getEmoji(minesweeperGame.getValueAt(i, j - 1)));
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainGUI::new);
    }
    private void addGameObserver(GameObserver observer) {
        observers.add(observer);
    }

    private void notifyGameObservers() {
        for (GameObserver observer : observers) {
            observer.gameWon();
        }
    }

    private void startTimer() {
        elapsedTime = 0;  // Сброс времени
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
        flagsLabel.setText("Флаги: " + String.valueOf(countFlags));
    }
}
