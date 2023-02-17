import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Client {
    private boolean isOpen = true;

    public void start() {
        InputsHandler input = new InputsHandler();
        String serverAddress = "";
        int port = 0;

        try {
            serverAddress = input.checkIp();
            port = input.checkPort();
        } catch (IOException e) {
            System.out.println(e);
        }

        try (
                // Création d'une nouvelle connexion aves le serveur
                Socket socket = new Socket(serverAddress, port);
                // Canal sortant pour envoyer des messages au serveur
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        ) {
            System.out.println("Serveur lancé sur [ " + serverAddress + " : " + port + " ]");

            MessageReceiver messageReceiver = new MessageReceiver(socket.getInputStream());

            messageReceiver.start();

            String userMessage;

            while (isOpen) {
                userMessage = stdIn.readLine();

                handleCommand(out, userMessage.split(" "));
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleCommand(DataOutputStream out, String[] arguments) throws IOException {
        if (arguments.length < 1) {
            return;
        }

        out.writeUTF(String.join(" ", arguments));

        switch (arguments[0]) {
            case "exit" -> isOpen = false;
            case "upload" -> sendFile(out, arguments);
        }
    }

    private void sendFile(DataOutputStream out, String[] arguments) {
        if (arguments.length < 2 || !fileExists(arguments[1])) {
            return;
        }

        System.out.println("Uploading " + arguments[1]);
    }

    private boolean fileExists(String path) {
        return Files.isRegularFile(Paths.get(path));
    }
}
