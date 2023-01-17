import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionListener extends Thread {
    private ConcurrentHashMap<Integer, ClientHandler> clients;

    private ConcurrentLinkedQueue<String> sharedMessagesQueue;

    public ConnectionListener(ConcurrentHashMap<Integer, ClientHandler> clients, ConcurrentLinkedQueue<String> sharedMessageQueue) {
        this.clients = clients;
        this.sharedMessagesQueue = sharedMessageQueue;
    }

    public void run() {
        int clientNumber = 0;
        // Compteur incrémenté à chaque connexion d'un client au serveur
        // Adresse et port du serveur
        String serverAddress = "127.0.0.1";
        int serverPort = 5000;
        // Création de la connexien pour communiquer ave les, clients
        try (
                ServerSocket Listener = new ServerSocket();
        ) {
            Listener.setReuseAddress(true);
            InetAddress serverIP = InetAddress.getByName(serverAddress);
            // Association de l'adresse et du port à la connexien
            Listener.bind(new InetSocketAddress(serverIP, serverPort));

            System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);

            while (true) {
                // Important : la fonction accept() est bloquante: attend qu'un prochain client se connecte
                // Une nouvetle connection : on incémente le compteur clientNumber
                ClientHandler client = new ClientHandler(Listener.accept(), clientNumber++, sharedMessagesQueue);
                clients.put(clientNumber - 1, client);
                client.start();
            }
        } catch (IOException e) {
            System.out.println("IOException : " + e.getMessage());
        }
    }
}
