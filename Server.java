import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 5129;
    // List to keep track of connected clients
    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

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

                System.out.println("[Server] " + playerName + " (Client #" + playerNumber + ") has joined the lobby. Total active clients: " + clients.size());
                out.println("LOGIN_SUCCESS: Welcome " + playerName + "! You are connected as Client #" + playerNumber + ".");

                // Exchange messages
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("[" + playerName + "]: " + message);
                    out.println("Server received from " + playerName + ": " + message);
                    
                    if (message.equalsIgnoreCase("quit")) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("[Server] Connection lost with " + (playerName != null ? playerName : "Client #" + playerNumber));
            } finally {
                clients.remove(this);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("[Server] " + (playerName != null ? playerName : "Client #" + playerNumber) + " disconnected. Active clients: " + clients.size());
            }
        }
    }
}