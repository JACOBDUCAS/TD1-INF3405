import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class AddressInputHandler {
    public static String getIp() throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        String ip;

        do {
            System.out.println("Enter the IP address: ");
            ip = input.readLine();
        } while (!IpRegexChecker.isValid(ip));

        return ip;
    }

    public static int getPort() {
        int port = 0;
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        do {
            System.out.println("Enter the port: ");

            try {
                port = Integer.parseInt(input.readLine());
            } catch (IOException | NumberFormatException e) {
                System.out.println("Enter a good port");
            }
        } while (port < 5000 || port > 5050);

        return port;
    }

}
