import java.io.PrintWriter;
import java.util.Random;

public class Room {
    public String players[] = new String[2];
    public Integer playerCount = 0;
    private String board[][] = new String[3][3];
    public Integer currentPlayer;
    private PrintWriter playerSocket[] = new PrintWriter[2];
    private String pieces[] = new String[] { "X", "O" };

    Room() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = "";
            }
        }
    }

    public void join(String player, PrintWriter out) {
        players[playerCount] = player;
        playerSocket[playerCount] = out;
        playerCount++;
    }

    // start the game
    // return the starting player
    public String start() {
        currentPlayer = new Random().nextInt(2);
        return players[currentPlayer];
    }

    // return on success status
    public void placeChess(String player, Integer posX, Integer posY) throws Exception {

        if (player != players[currentPlayer]) {
            throw new Exception("Not your turn, please wait for your opponent.");
        }

        if (board[posX][posY] != "") {
            throw new Exception("This place has already been taken");
        }

        board[posX][posY] = player;

        // broadcast the chess to everyone in the room
        for (int i = 0; i < 2; i++) {
            playerSocket[i].println("valid:" + player + " has placed the chess" + ":" + posX + "," + posY + ":"
                    + pieces[currentPlayer]);
        }

        currentPlayer = (currentPlayer + 1) % 2;
    }

    public void checkWinning(String player) {
        if (checkStatus(player)) {
            for (int i = 0; i < 2; i++) {
                playerSocket[i].println("end:" + player + " has won the game!");
            }
        }
    }

    // check if the player has won the game
    private boolean checkStatus(String player) {
        Integer i;
        // check vertical
        for (i = 0; i < 3; i++) {
            if (board[i][0] == player) {
                if (board[i][1] == player) {
                    if (board[i][2] == player) {
                        return true;
                    }
                }
            }
        }

        // check horizontal
        for (i = 0; i < 3; i++) {
            if (board[0][i] == player) {
                if (board[1][i] == player) {
                    if (board[2][i] == player) {
                        return true;
                    }
                }
            }
        }

        // check diagonal
        if (board[0][0] == player) {
            if (board[1][1] == player) {
                if (board[2][2] == player) {
                    return true;
                }
            }
        }

        if (board[2][0] == player) {
            if (board[1][1] == player) {
                if (board[0][2] == player) {
                    return true;
                }
            }
        }

        return false;
    }
}
