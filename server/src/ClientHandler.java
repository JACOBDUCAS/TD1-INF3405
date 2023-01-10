import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler extends Thread { // pour traiter la demande de chaque client sur un socket particulier
    private Socket socket;
    private int clientNumber;

    public ClientHandler(Socket socket, int clientNumber) {
        this.socket = socket;
        this.clientNumber = clientNumber;
        System.out.println("New connection with client#" + clientNumber + " at" + socket);
    }

    public void run() { // Création de thread qui envoi un message à un client
        try (
                DataOutputStream out = new DataOutputStream(socket.getOutputStream()); // création de canal d’envoi
                DataInputStream in = new DataInputStream(socket.getInputStream());
                ) {
            out.writeUTF("Hello from server - you are client#" + clientNumber); // envoi de message

            while (true) {
                if (in.available() > 0) {
                    String message = in.readUTF();

                    if (message.equals("EXIT")) {
                        break;
                    } else {
                        System.out.println("Client #" + clientNumber + " : " + message);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error handling client# " + clientNumber + ": " + e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Couldn't close a socket, what's going on?");
            }
            System.out.println("Connection with client# " + clientNumber + " closed");
        }
    }
}