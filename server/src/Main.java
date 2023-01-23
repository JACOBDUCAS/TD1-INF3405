import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Main {

    private static ConcurrentLinkedQueue<String> sharedMessagesQueue;

    private static ConcurrentHashMap<Integer, ClientHandler> clients;

    private static boolean clientDisconnected(int clientNumber, String message) {
        return message.contains("EXIT") && new Scanner(message).useDelimiter("\\D+").nextInt() == clientNumber;
    }

    public static void main(String[] args) throws Exception {
        sharedMessagesQueue = new ConcurrentLinkedQueue<>();
        clients = new ConcurrentHashMap<>();

        ConnectionListener cl = new ConnectionListener(clients, sharedMessagesQueue);

        cl.start();

        // À chaque fois qu'un nouveau client se, connecte, on exécute la fonction run() de l'objet ClientHandler
        while (true) {
            while (!sharedMessagesQueue.isEmpty()) {
                String message = sharedMessagesQueue.poll();
                System.out.println(message);

                for (ClientHandler ch : clients.values()) {
                    ch.addMessage(message);

                    if (clientDisconnected(ch.getClientNumber(), message)) {
                        clients.remove(ch.getClientNumber());
                    }
                }
            }
        }
    }
}