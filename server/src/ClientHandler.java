import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler extends Thread { // pour traiter la demande de chaque client sur un socket particulier
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        System.out.println(socket.getRemoteSocketAddress() + " connected.");
    }

    public void run() { // Création de thread qui envoi un message à un client
        try (
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());
        ) {
            out.writeUTF("Hello from the server.");
            
            while (true) {
                if (in.available() > 0) {
                    String message = in.readUTF();

                    System.out.println(socket.getRemoteSocketAddress() + " : " + message);

                    if (message.startsWith("exit")) {
                        System.out.println(socket.getRemoteSocketAddress() + " disconnected.");
                        break;
                    }
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