import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MessageReceiver extends Thread {
    private InputStream inputStream;

    public MessageReceiver(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        try (
                // Céatien d'un canal entrant pour recevoir les messages envoyés, par le serveur
                DataInputStream in = new DataInputStream(inputStream)
        ) {
            while (true) {
                if (in.available() > 0) {
                    // Attente de la réception d'un message envoyé par le, server sur le canal
                    String message = in.readUTF();

                    if (message.startsWith("Downloading")) {
                        downloadFile(in, message.split(" ")[1]);
                    }

                    System.out.println(message);
                }
            }
        } catch (IOException e) {
            System.out.println("IOException : " + e.getMessage());
        }
    }

    // Adapted from https://heptadecane.medium.com/file-transfer-via-java-sockets-e8d4f30703a5
    private void downloadFile(DataInputStream in, String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            long bytesToRead = in.readLong();

            byte[] fileBuffer = new byte[Client.FILE_BUFFER_SIZE];

            // FIXME: Make sure we don't get stuck in the loop if the file isn't actually sent
            while (bytesToRead > 0) {
                int bytesRead = in.read(fileBuffer);

                if (bytesRead == -1) {
                    break;
                }

                fos.write(fileBuffer, 0, bytesRead);
                bytesToRead -= bytesRead;
            }
        } catch (IOException e) {
            System.out.println("MessageReceiver.downloadFile : " + e.getMessage());
        }
    }
}
