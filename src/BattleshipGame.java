

import java.util.Arrays;

public class BattleshipGame {
    private final char[][] player1Board;
    private final char[][] player2Board;
    private int currentTurn; 
    private int p1Hits;
    private int p2Hits;
    private static final int WINNING_HITS = 17; 

    public BattleshipGame() {
        player1Board = new char[10][10];
        player2Board = new char[10][10];
        initializeBoard(player1Board);
        initializeBoard(player2Board);
        this.currentTurn = 1;
        this.p1Hits = 0;
        this.p2Hits = 0;
        
        
        placeDefaultShips(player1Board);
        placeDefaultShips(player2Board);
    }

    private void initializeBoard(char[][] board) {
        for (char[] row : board) {
            Arrays.fill(row, '~'); 
        }
    }

    
    private void placeDefaultShips(char[][] board) {
        
        for (int i = 0; i < 5; i++) board[0][i] = 'S';
        
        for (int i = 1; i < 5; i++) board[2][i] = 'S';
        
        for (int i = 5; i < 8; i++) board[4][i] = 'S';
        
        for (int i = 2; i < 5; i++) board[6][i] = 'S';
        
        for (int i = 7; i < 9; i++) board[8][i] = 'S';
    }

    public synchronized String processAttack(int playerNumber, int row, int col) {
        
        if (playerNumber != currentTurn) {
            return "{\"status\": \"ERROR\", \"message\": \"Not your turn!\"}";
        }

        
        if (row < 0 || row >= 10 || col < 0 || col >= 10) {
            return "{\"status\": \"ERROR\", \"message\": \"Coordinates out of bounds! Use A-J and 1-10.\"}";
        }

        
        char[][] targetBoard = (playerNumber == 1) ? player2Board : player1Board;
        char targetCell = targetBoard[row][col];

        if (targetCell == 'X' || targetCell == 'O') {
            return "{\"status\": \"RETRY\", \"message\": \"You already fired at this coordinate! Try another spot.\"}";
        }

        String result;
        if (targetCell == 'S') {
            targetBoard[row][col] = 'X'; 
            if (playerNumber == 1) p1Hits++;
            else p2Hits++;

            if (hasWon(playerNumber)) {
                result = "{\"status\": \"WIN\", \"message\": \"HIT! All enemy ships destroyed. You Win!\"}";
            } else {
                result = "{\"status\": \"HIT\", \"message\": \"HIT!\"}";
                switchTurn();
            }
        } else {
            targetBoard[row][col] = 'O'; 
            result = "{\"status\": \"MISS\", \"message\": \"MISS!\"}";
            switchTurn();
        }

        return result;
    }

    private void switchTurn() {
        currentTurn = (currentTurn == 1) ? 2 : 1;
    }

    private boolean hasWon(int playerNumber) {
        return (playerNumber == 1 && p1Hits >= WINNING_HITS) || (playerNumber == 2 && p2Hits >= WINNING_HITS);
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    
    public String getBoardView(char[][] board, boolean hideShips) {
        StringBuilder sb = new StringBuilder("  1 2 3 4 5 6 7 8 9 10\n");
        char rowLabel = 'A';
        for (char[] row : board) {
            sb.append(rowLabel++).append(" ");
            for (char cell : row) {
                if (hideShips && cell == 'S') {
                    sb.append("~ ");
                } else {
                    sb.append(cell).append(" ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}