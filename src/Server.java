import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 5129;
    // List to keep track of connected clients
    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private static BattleshipGame game = new BattleshipGame();

    public static void main(String[] args) {
        System.out.println("[Server] Battleship Server starting on port " + PORT + "...");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            int clientCount = 0;
            
            while (true) {
                // Wait for a client to connect
                Socket clientSocket = serverSocket.accept();
                clientCount++;
                System.out.println("[Server] Connection accepted from " + clientSocket.getRemoteSocketAddress());

                // Create a new thread for this client so others don't have to wait
                ClientHandler handler = new ClientHandler(clientSocket, clientCount);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.out.println("[Server] Error: " + e.getMessage());
        }
    }

    // Class to handle each client on a separate thread
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private int playerNumber;
        private String playerName;

        public ClientHandler(Socket socket, int playerNumber) {
            this.socket = socket;
            this.playerNumber = playerNumber;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Basic login/join step
                out.println("Welcome to Battleship! Please enter your username to join:");
                playerName = in.readLine();
                
                if (playerName == null || playerName.trim().isEmpty()) {
                    playerName = "Player" + playerNumber;
                }

                System.out.println("[Server] " + playerName + " (Player " + playerNumber + ") has joined the lobby. Total active clients: " + clients.size());
                out.println("LOGIN_SUCCESS: Welcome " + playerName + "! You are connected as Player " + playerNumber + ".");

                // Game message loop
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("[" + playerName + " (P" + playerNumber + ")]: " + message);
                    
                    if (message.equalsIgnoreCase("quit")) {
                        break;
                    }

                    // Parse incoming attack coordinates formatted as "row,col" (e.g., 0,0 or 2,1)
                    String[] parts = message.split(",");
                    if (parts.length == 2) {
                        try {
                            int row = Integer.parseInt(parts[0].trim());
                            int col = Integer.parseInt(parts[1].trim());
                            
                            // Pass the move to the shared game instance
                            String gameResponse = game.processAttack(playerNumber, row, col);
                            
                            // Send the hit/miss/win result back to the client
                            out.println(gameResponse);
                            
                        } catch (NumberFormatException e) {
                            out.println("{\"status\": \"ERROR\", \"message\": \"Invalid format. Use row,col (e.g., 1,3)\"}");
                        }
                    } else {
                        out.println("{\"status\": \"ERROR\", \"message\": \"Please enter coordinates as row,col\"}");
                    }
                }
            } catch (IOException e) {
                System.out.println("[Server] Connection lost with " + (playerName != null ? playerName : "Player #" + playerNumber));
            } finally {
                clients.remove(this);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("[Server] " + (playerName != null ? playerName : "Player #" + playerNumber) + " disconnected. Active clients: " + clients.size());
            }
        }
    }
}