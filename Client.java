import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 5129;

    public static void main(String[] args) {
        System.out.println("[Client] Connecting to Battleship server at " + SERVER_ADDRESS + ":" + PORT + "...");

        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("[Client] Connected to the server successfully!");

            String welcomeMsg = in.readLine();
            if (welcomeMsg != null) {
                System.out.println(welcomeMsg);
            }

            System.out.print("Your username: ");
            String username = scanner.nextLine();
            out.println(username);

            String loginResponse = in.readLine();
            if (loginResponse != null) {
                System.out.println(loginResponse);
            }

            Thread listenerThread = new Thread(() -> {
                try {
                    String incomingMessage;
                    while ((incomingMessage = in.readLine()) != null) {
                        System.out.println("\n[Server Message]: " + incomingMessage);
                        System.out.print("> ");
                    }
                } catch (IOException e) {
                    System.out.println("\n[Client] Disconnected from server.");
                }
            });
            listenerThread.start();

            while (true) {
                System.out.print("> ");
                String userInput = scanner.nextLine();
                out.println(userInput);

                if (userInput.equalsIgnoreCase("quit")) {
                    System.out.println("[Client] Exiting chat and closing connection...");
                    break;
                }
            }

        } catch (UnknownHostException e) {
            System.out.println("[Client] Error: Unknown host " + SERVER_ADDRESS);
        } catch (IOException e) {
            System.out.println("[Client] Error: Could not connect to server. Ensure Server.java is running on port " + PORT + ".");
        }
    }
}
