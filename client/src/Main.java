import java.io.*;
import java.net.Socket;
import java.util.Objects;


// Application client
public class Main {
    private static Socket socket;

    public static void main(String[] args) throws Exception {
        // Adresse et port du serveur


        InputsHandler  input = new InputsHandler();
        String serverAddress = input.checkIp();
        int port = input.checkPort();
        try (
                // Création d'une nouvelle connexion aves le serveur
                Socket socket = new Socket(serverAddress, port);
                // Canal sortant pour envoyer des messages au serveur
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        ) {
            System.out.println("Serveur lancé sur [" + serverAddress + ":" + port + "]");

            MessageReceiver messageReceiver = new MessageReceiver(socket.getInputStream());

            messageReceiver.start();

            String userMessage;
            while (!Objects.equals(userMessage = stdIn.readLine(), "EXIT")) {
                if (userMessage.length() > 0) {
                    out.writeUTF(userMessage);

                }
            }

            out.writeUTF(userMessage);
        }
    }
}