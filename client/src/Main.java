import java.io.*;
import java.net.Socket;
import java.util.Objects;

// Application client
public class Main {
    private static Socket socket;

    public static void main(String[] args) throws Exception {
        // Adresse et port du serveur
        String serverAddress = "127.0.0.1";
        int port = 5000;
        try (
                // Création d'une nouvelle connexion aves le serveur
                Socket socket = new Socket(serverAddress, port);
                // Céatien d'un canal entrant pour recevoir les messages envoyés, par le serveur
                DataInputStream in = new DataInputStream(socket.getInputStream());
                // Canal sortant pour envoyer des messages au serveur
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        ) {
            System.out.println("Serveur lancé sur [" + serverAddress + ":" + port + "]");

            out.writeUTF("Hello from Client");

            String userMessage;
            while (!Objects.equals(userMessage = stdIn.readLine(), "EXIT")) {
                if (in.available() > 0) {
                    // Attente de la réception d'un message envoyé par le, server sur le canal
                    String message = in.readUTF();
                    System.out.println(message);
                }

                if (userMessage.length() > 0) {
                    out.writeUTF(userMessage);
                }
            }

            out.writeUTF(userMessage);
        }
    }
}