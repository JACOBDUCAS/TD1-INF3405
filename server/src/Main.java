import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Main {
    private static ServerSocket Listener; // Application Serveur

    private static ConcurrentLinkedQueue<String> sharedMessagesQueue;

    private static HashMap<Integer, ClientHandler> clients;

    private static boolean clientDisconnected(int clientNumber, String message) {
        return message.contains("EXIT") && new Scanner(message).useDelimiter("\\D+").nextInt() == clientNumber;
    }

    public static void main(String[] args) throws Exception {
        sharedMessagesQueue = new ConcurrentLinkedQueue<>();
        clients = new HashMap();
        // Compteur incrémenté à chaque connexion d'un client au serveur
        int clientNumber = 0;
        // Adresse et port du serveur
        String serverAddress = "127.0.0.1";
        int serverPort = 5000;
        // Création de la connexien pour communiquer ave les, clients
        Listener = new ServerSocket();
        Listener.setReuseAddress(true);
        InetAddress serverIP = InetAddress.getByName(serverAddress);
        // Association de l'adresse et du port à la connexien
        Listener.bind(new InetSocketAddress(serverIP, serverPort));
        System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);
        try {
            // À chaque fois qu'un nouveau client se, connecte, on exécute la fonction run() de l'objet ClientHandler
            while (true) {
                // Important : la fonction accept() est bloquante: attend qu'un prochain client se connecte
                // Une nouvetle connection : on incémente le compteur clientNumber
                ClientHandler client = new ClientHandler(Listener.accept(), clientNumber++, sharedMessagesQueue);
                clients.put(clientNumber - 1, client);
                client.start();

                while (!sharedMessagesQueue.isEmpty()) {
                    String message = sharedMessagesQueue.poll();
                    System.out.println(message);

                    for (ClientHandler ch : clients.values()) {
                        ch.addMessage(message);

                        if (clientDisconnected(client.getClientNumber(), message)) {
                            clients.remove(client.getClientNumber());
                        }
                    }
                }
            }
        } finally {
            // Fermeture de la connexion
            Listener.close();
        }
    }
}