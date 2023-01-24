import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class ClientHandler extends Thread { // pour traiter la demande de chaque client sur un socket particulier
    private Socket socket;
    private boolean isOpen;

    private Queue<String> stringOutputBuffer;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.isOpen = true;
        this.stringOutputBuffer = new LinkedList<>();
        System.out.println(socket.getRemoteSocketAddress() + " connected.");
    }

    private void changeDirectory(String directory) {
        System.out.println("Changing directory to " + directory);
    }

    private void list() {
        System.out.println("Listing current directory");
    }

    private void createDirectory(String directoryName) {
        System.out.println("Creating directory " + directoryName);
    }

    private void upload(String fileName) {
        System.out.println("Uploaded " + fileName);
    }

    private void download(String fileName) {
        System.out.println("Downloaded " + fileName);
    }

    private void exit() {
        System.out.println(socket.getRemoteSocketAddress() + " disconnected.");
        isOpen = false;
    }

    private void handleCommand(String[] arguments) {
        switch (arguments[0]) {
            case "cd" -> changeDirectory(arguments[1]);
            case "ls" -> list();
            case "mkdir" -> createDirectory(arguments[1]);
            case "upload" -> upload(arguments[1]);
            case "download" -> download(arguments[1]);
            case "exit" -> exit();
            default -> System.out.println("Command not recognized : " + Arrays.toString(arguments));
        }
    }

    public void run() { // Création de thread qui envoi un message à un client
        try (
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());
        ) {
            stringOutputBuffer.offer("Hello from the server.");
            stringOutputBuffer.offer("Second message");

            while (isOpen) {
                if (in.available() > 0) {
                    String message = in.readUTF().trim();

                    System.out.println(socket.getRemoteSocketAddress() + " : " + message);

                    // FIXME: Handle the case where the user sent an invalid number of arguments or bad input
                    handleCommand(message.split(" "));
                }

                while (!stringOutputBuffer.isEmpty()) {
                    out.writeUTF(stringOutputBuffer.poll());
                }
            }
        } catch (IOException e) {
            System.out.println("Error handling client");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Couldn't close a socket, what's going on?");
            }
        }
    }
}