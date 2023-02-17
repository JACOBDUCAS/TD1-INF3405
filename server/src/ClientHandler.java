import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ClientHandler extends Thread {
    // pour traiter la demande de chaque client sur un socket particulier
    private static final int FILE_BUFFER_SIZE = 2048;

    private final Socket socket;
    private boolean isOpen;

    private final Queue<String> stringOutputBuffer;

    private String workingDirectory;

    private DataInputStream dataInputStream;

    private DataOutputStream dataOutputStream;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.isOpen = true;
        this.stringOutputBuffer = new LinkedList<>();
        this.workingDirectory = "./";
        System.out.println(socket.getRemoteSocketAddress() + " connected.");
    }

    private void changeDirectory(String directory) {
        String newPath = workingDirectory + directory + "/";
        if (directoryExists(newPath)) {
            workingDirectory = newPath;
            stringOutputBuffer.offer("Changing directory to " + directory);
        } else {
            stringOutputBuffer.offer("Cannot change to " + directory);
        }
    }

    private boolean directoryExists(String path) {
        return Files.isDirectory(Paths.get(path));
    }

    private void list() {
        File currentDir = new File(String.valueOf(Paths.get(workingDirectory)));
        File[] directoryFiles = currentDir.listFiles();

        if (directoryFiles == null) {
            stringOutputBuffer.offer("Directory is empty.");
            return;
        }

        for (File file : directoryFiles) {
            String fileMessage = file.getName();

            if (file.isDirectory()) {
                fileMessage += "/";
            }

            stringOutputBuffer.offer(fileMessage);
        }
    }

    private void createDirectory(String directoryName) {
        // FIXME: Test to handle invalid paths
        Path path = Paths.get(workingDirectory + directoryName);
        try {
            Files.createDirectory(path);
            stringOutputBuffer.offer("Created directory " + directoryName);
        } catch (IOException e) {
            // FIXME: Handle this properly, handle FileAlreadyExistsException for more
            // specific error message
            System.out.println("IOException: " + e.getMessage());
            stringOutputBuffer.offer("Failed to create directory " + directoryName);
        }
    }

    // Adapted from https://heptadecane.medium.com/file-transfer-via-java-sockets-e8d4f30703a5
    private void upload(String fileName) {
        String destinationPath = workingDirectory + fileName;

        try (FileOutputStream fos = new FileOutputStream(destinationPath)) {
            long bytesToRead = dataInputStream.readLong();

            byte[] fileBuffer = new byte[FILE_BUFFER_SIZE];

            // FIXME: Make sure we don't get stuck in the loop if the file isn't actually sent
            while (bytesToRead > 0) {
                int bytesRead = dataInputStream.read(fileBuffer);

                if (bytesRead == -1) {
                    break;
                }

                fos.write(fileBuffer, 0, bytesRead);
                bytesToRead -= bytesRead;
            }
        } catch (IOException e) {
            System.out.println("ClientHandler.upload : " + e.getMessage());
        }
    }

    private void download(String fileName) throws IOException {
        if (!fileExists(fileName)) {
            dataOutputStream.writeUTF("Invalid file name");
            return;
        }

        File file = Paths.get(fileName).toFile();

        // Adapted from https://heptadecane.medium.com/file-transfer-via-java-sockets-e8d4f30703a5
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            dataOutputStream.writeUTF("Downloading " + fileName);

            long fileSize = file.length();

            dataOutputStream.writeLong(fileSize);

            byte[] fileBuffer = new byte[FILE_BUFFER_SIZE];
            int nbBytesToWrite = 0;

            while ((nbBytesToWrite = fileInputStream.read(fileBuffer)) != -1) {
                dataOutputStream.write(fileBuffer, 0, nbBytesToWrite);
                dataOutputStream.flush();
            }
        } catch (IOException e) {
            System.out.println("ClientHandler.download : " + e.getMessage());
        }
        System.out.println("Downloaded " + fileName);
    }

    private boolean fileExists(String path) {
        return Files.isRegularFile(Paths.get(path));
    }

    private void exit() {
        System.out.println(socket.getRemoteSocketAddress() + " disconnected.");
        isOpen = false;
    }

    private void handleCommand(String[] arguments) throws IOException {
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

    private void logCommand(String message) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd @ HH:mm:ss");
        LocalDateTime time = LocalDateTime.now();

        String logMessage = "[" +
                socket.getRemoteSocketAddress() +
                " - " +
                dateTimeFormatter.format(time) +
                "]" +
                ": " +
                message;

        System.out.println(logMessage);
    }

    public void run() { // Création de thread qui envoi un message à un client
        try (
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream())) {
            while (isOpen) {
                dataOutputStream = out;
                dataInputStream = in;

                if (in.available() > 0) {
                    String message = in.readUTF().trim();

                    logCommand(message);

                    // FIXME: Handle the case where the user sent an invalid number of arguments or
                    // bad input
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