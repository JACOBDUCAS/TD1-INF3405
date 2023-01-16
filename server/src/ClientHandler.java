import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientHandler extends Thread { // pour traiter la demande de chaque client sur un socket particulier
    private Socket socket;
    private int clientNumber;

    private ConcurrentLinkedQueue<String> threadMessageQueue;

    private ConcurrentLinkedQueue<String> sharedMessageQueue;

    public ClientHandler(Socket socket, int clientNumber, ConcurrentLinkedQueue<String> messageQueue) {
        this.socket = socket;
        this.clientNumber = clientNumber;
        this.sharedMessageQueue = messageQueue;
        this.threadMessageQueue = new ConcurrentLinkedQueue<>();
        System.out.println("New connection with client#" + clientNumber + " at" + socket);
    }

    public int getClientNumber() {
        return clientNumber;
    }

    public void addMessage(String message) {
        threadMessageQueue.offer(message);
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

                    message = "Client #" + clientNumber + " : " + message;
                    sharedMessageQueue.offer(message);
                    if (message.contains("EXIT")) {
                        break;
                    }
                }

                while (!threadMessageQueue.isEmpty()) {
                    String outboundMessage = threadMessageQueue.poll();
                    out.writeUTF(outboundMessage);
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