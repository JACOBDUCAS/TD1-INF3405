import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server {
    private ConcurrentLinkedQueue<String> sharedMessagesQueue;

    private ConcurrentHashMap<Integer, ClientHandler> clients;

    public Server() {
        this.sharedMessagesQueue = new ConcurrentLinkedQueue<>();
        this.clients = new ConcurrentHashMap<>();
    }

    public void start() {
        ConnectionListener cl = new ConnectionListener(clients, sharedMessagesQueue);

        cl.start();

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

    private boolean clientDisconnected(int clientNumber, String message) {
        return message.contains("EXIT") && new Scanner(message).useDelimiter("\\D+").nextInt() == clientNumber;
    }


}
