import java.util.Random;

class DefaultAdjacentMinesCounter implements AdjacentMinesCounter {
    @Override
    public int countAdjacentMines(int[][] board, int row, int col, int boardSize) {
        int count = 0;
        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = col - 1; j <= col + 1; j++) {
                if (i >= 0 && i < boardSize && j >= 0 && j < boardSize && board[i][j] == -1) {
                    count++;
                }
            }
        }
        return count;
    }
}

public class MinesweeperGame implements MinesweeperBoard {

    private static MinesweeperGame instance;
    private int[][] minesweeperBoard;
    private boolean[][] flags;
    private int boardSize;
    private int countMines;
    private int countFlags;
    private boolean isNext = true;
    private AdjacentMinesCounter adjacentMinesCounter;

    public static MinesweeperGame getInstance(int boardSize, int countMines){
        if (instance == null) {
            instance = new MinesweeperGame(boardSize,countMines);
        }
        return instance;
    }
    private MinesweeperGame(int boardSize, int countMines) {
        this.boardSize = boardSize;
        this.countMines = countMines;
        this.adjacentMinesCounter = new DefaultAdjacentMinesCounter();
        initializeBoard();
    }

    @Override
    public int getBoardSize() {
        return boardSize;
    }

    @Override
    public int getValueAt(int row, int col) {
        return minesweeperBoard[row][col];
    }

    @Override
    public void initializeBoard() {
        int nowCountMines = 0;
        minesweeperBoard = new int[boardSize][boardSize];
        this.flags = new boolean[boardSize][boardSize];
        Random random = new Random();
        while (nowCountMines < countMines) {
            int row = random.nextInt(boardSize);
            int col = random.nextInt(boardSize);

            if (minesweeperBoard[row][col] != -1) {
                minesweeperBoard[row][col] = -1;
                nowCountMines++;
            }
        }

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (minesweeperBoard[i][j] != -1) {
                    minesweeperBoard[i][j] = countAdjacentMines(i, j);
                }
            }
        }
        PrintArray();

    }
    // TEST FUNCTION
    private void PrintArray(){
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                System.out.printf(String.valueOf(minesweeperBoard[i][j])+ " ");
            }
            System.out.printf("\n");
        }
        System.out.printf("\n");
    }

    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = col - 1; j <= col + 1; j++) {
                if (i >= 0 && i < boardSize && j >= 0 && j < boardSize && minesweeperBoard[i][j] == -1) {
                    count++;
                }
            }
        }
        return count;
    }

    public void openCell(int row, int col) {
        if (!flags[row][col] && minesweeperBoard[row][col] != -1) {
            isNext = true;
            openEmptyCells(row, col);
        }
        PrintArray();
    }


    private void openEmptyCells(int row, int col) {
        if (row < 0 || row >= boardSize || col < 0 || col >= boardSize || minesweeperBoard[row][col] != 0) {
            isNext = false;
            return;
        }

        if (minesweeperBoard[row][col] == 0) {
            minesweeperBoard[row][col] = -2;

            if(isNext) {
                openEmptyCells(row - 1, col);
                openEmptyCells(row + 1, col);
                openEmptyCells(row, col - 1);
                openEmptyCells(row, col + 1);
            }
        } else if (minesweeperBoard[row][col] > 0) {
            minesweeperBoard[row][col] = -2;
        }
    }

    public void setFlag(int row, int col) {
        countFlags++;
        this.flags[row][col] = true;
    }

    public void removeFlag(int row, int col) {
        this.flags[row][col] = false;
    }

    public boolean getFlag(int row, int col){
        return this.flags[row][col];
    }

    public void setAdjacentMinesCounter(AdjacentMinesCounter counter) {
        this.adjacentMinesCounter = counter;
    }
    public void setBorderSize(int size) {
        this.boardSize = size;
        this.countMines = size-2;
    }
    public boolean isGameWon() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (minesweeperBoard[i][j] == 0) {
                    return false;
                }
            }
        }
        return countFlags == countMines;
    }

    public int getMinesCount() {
        return countMines;
    }
}
