import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server {
    public static void main(String[] args) throws Exception {
        try (ServerSocket listener = new ServerSocket(59898)) {
            System.out.println("The server is starting");
            while (true) {
                Socket newSocket = listener.accept();
                GameThread thread = new GameThread(newSocket);
                thread.start();
            }
        } catch (Exception error) {
            System.err.println(error.getMessage());
        }
    }

    private static class GameThread extends Thread {
        private static ArrayList<Room> rooms = new ArrayList<Room>();
        private Socket socket;
        private String user;

        GameThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            Room room;

            System.out.println("Connected: " + socket);
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                String clientRequest;
                while (true) {
                    System.out.println("Server: Waiting for input..");
                    clientRequest = in.readLine();
                    System.out.println(clientRequest);
                    String[] request = clientRequest.split(":");
                    switch (request[0]) {
                    case "join":
                        synchronized (this) {
                            System.out.println(rooms);

                            // check if the latest room has slot, if not, open a new room and join the room
                            // is full
                            if (rooms.size() == 0) {
                                System.out.println("No available room, creating one");
                                Room newRoom = new Room();
                                rooms.add(newRoom);
                            }
                            if (rooms.get(rooms.size() - 1).playerCount == 2) {
                                System.out.println("Room full, creating one");
                                Room newRoom = new Room();
                                rooms.add(newRoom);
                            }

                            // select the head of the queue
                            room = rooms.get(rooms.size() - 1);

                            // add the player into the room
                            room.join(request[1], out);
                            System.out.println(room.playerCount);

                            // return the room number
                            out.println("room:" + (rooms.size() - 1));

                            user = request[1];
                        }
                        String currentPlayer;
                        if (room.playerCount == 2) {
                            currentPlayer = room.start();

                        } else {
                            // wait for players
                            while (room.playerCount != 2) {
                                System.out.println(room);
                                System.out.println(room.playerCount);
                                Thread.sleep(500);
                            }
                            currentPlayer = room.players[room.currentPlayer];
                        }
                        out.println("start:The game has started, " + currentPlayer + " will go first");
                        break;

                    case "piece":
                        room = rooms.get(Integer.parseInt(request[1]));
                        String pos[] = request[2].split(",");
                        try {
                            room.placeChess(user, Integer.parseInt(pos[0]), Integer.parseInt(pos[1]));
                            room.checkWinning(user);
                        } catch (Exception error) {
                            out.println("invalid:" +error.getMessage());
                        }
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error:" + socket);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                }
                System.out.println("Closed: " + socket);
            }
        }
    }
}