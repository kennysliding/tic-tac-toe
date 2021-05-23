import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Client {

    private JFrame frame;
    private JPanel MainPanel, GamePanel, OptionPanel;
    private JLabel labelInfo, nameInstruction;
    private JTextField textName;
    private Chess chess[][];
    private JButton btnSubmitName;

    Thread socketThread;
    String default_host = "127.0.0.1";
    Socket socket;
    PrintWriter out;
    BufferedReader in;

    private String roomCode;

    public static void main(String[] args) throws Exception {
        System.out.println("Starting client");
        new Client();
    }

    public Client() {
        frame = new JFrame("Tic Tac Toe");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // open in the center of the screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);

        // menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu controlMenu = new JMenu("Control");
        JMenuItem quitItem = new JMenuItem("Exit");
        quitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        controlMenu.add(quitItem);
        JMenu helpMenu = new JMenu("Help");
        helpMenu.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mousePressed(MouseEvent e) {
                showPopup("Instructions",
                        "<html>Criteria for a valid move<br>- The move is not occupied by any mark<br>- The move is made in the player's turn<br>- The move is made within the 3 x 3 board<br><br>THe game would continue and switch among the opposite player until it reaches either one of the following conditions:<br>- Player 1 wins<br>- Player 2 wins<br>- Draw</html>");
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub
            }
        });
        menuBar.add(controlMenu);
        menuBar.add(helpMenu);
        frame.setJMenuBar(menuBar);

        GamePanel = new JPanel();
        GamePanel.setLayout(new GridLayout(3, 3));
        chess = new Chess[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                final Integer chessPositionX = i;
                final Integer chessPositionY = j;
                chess[i][j] = new Chess(chessPositionX, chessPositionY);
                chess[i][j].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        // send the click action to server
                        out.println("piece:" + roomCode + ":" + chessPositionX + "," + chessPositionY);
                    }
                });
                GamePanel.add(chess[i][j]);
            }
        }

        OptionPanel = new JPanel();
        nameInstruction = new JLabel("Name: ");
        textName = new JTextField(20);
        textName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                socketThread.start();
            }
        });
        labelInfo = new JLabel();
        btnSubmitName = new JButton("Submit");
        btnSubmitName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                socketThread.start();
            }
        });
        OptionPanel.add(nameInstruction);
        OptionPanel.add(textName);
        OptionPanel.add(btnSubmitName);
        OptionPanel.add(labelInfo);

        MainPanel = new JPanel();
        MainPanel.setLayout(new GridLayout(2, 1));
        MainPanel.add(GamePanel);
        MainPanel.add(OptionPanel);
        frame.add(MainPanel);
        frame.setSize(600, 800);
        frame.setVisible(true);

        showStartScreen();

        socketThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    connectSocket(textName.getText());
                } catch (UnknownHostException error) {
                    error.printStackTrace();
                } catch (IOException error) {
                    error.printStackTrace();
                }
            }
        });
    }

    public void connectSocket(String user) throws UnknownHostException, IOException {
        socket = new Socket(default_host, 59898);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // join the game
        out.println("join:" + user);

        String serverResponse;
        while (true) {
            try {
                serverResponse = in.readLine();
                System.out.println("Server - " + serverResponse); // show server response
                String[] response = serverResponse.split(":");

                switch (response[0]) {
                case "room":
                    System.out.println("joining room");
                    roomCode = response[1];
                    showInformation("You have joined room: " + roomCode + ", please wait for opponent");
                    showWaitingScreen();
                    break;

                case "start":
                    showInformation(response[1]);
                    showGameScreen();
                    break;

                case "valid":
                    showInformation(response[1]);
                    String[] pos = response[2].split(",");
                    chess[Integer.parseInt(pos[0])][Integer.parseInt(pos[1])].setPiece(response[3]);
                    break;

                case "invalid":
                    showInformation(response[1]);
                    break;

                case "end":
                    showInformation(response[1]);
                    // showStartScreen();
                    showPopup("The game has ended", response[1]);
                    break;
                }
            } catch (IOException error) {
                System.out.println("Receiving error: " + error.getMessage());
                break;
            }
        }
        out.close();
        in.close();
        socket.close();
    }

    public void showStartScreen() {
        showInformation("Submit your name to get started.");
        btnSubmitName.setEnabled(true);
        textName.setEditable(true);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                chess[i][j].setEnabled(false);
            }
        }
    }

    public void showWaitingScreen() {
        btnSubmitName.setEnabled(false);
        textName.setEditable(false);
    }

    public void showGameScreen() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                chess[i][j].resetPiece();
            }
        }
    }

    public void showPopup(String title, String message) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public void showInformation(String message) {
        labelInfo.setText(message);
        OptionPanel.updateUI();
    }
}
