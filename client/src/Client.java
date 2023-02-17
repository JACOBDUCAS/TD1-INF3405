import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Client {
    private static final int FILE_BUFFER_SIZE = 2048;
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
            System.out.println("File is not valid");
            return;
        }

        File file = Paths.get(arguments[1]).toFile();

        // Adapted from https://heptadecane.medium.com/file-transfer-via-java-sockets-e8d4f30703a5
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            long fileSize = file.length();

            out.writeLong(fileSize);

            byte[] fileBuffer = new byte[FILE_BUFFER_SIZE];
            int nbBytesToWrite = 0;

            System.out.println("Uploading " + arguments[1]);

            while ((nbBytesToWrite = fileInputStream.read(fileBuffer)) != -1) {
                out.write(fileBuffer, 0, nbBytesToWrite);
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("Client.sendFile : " + e.getMessage());
        }
    }

    private boolean fileExists(String path) {
        return Files.isRegularFile(Paths.get(path));
    }
}
