import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class FileUploadServer {
    
    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(5001);
            while(true) {
            Socket s = ss.accept();
            InputStream is = s.getInputStream();
            OutputStream os = s.getOutputStream();
            Scanner sc = new Scanner(is);
            PrintWriter pw = new PrintWriter(os);
            String command = sc.nextLine();
            String fileName = command.substring(5);
            File f = new File(fileName);
            if(f.exists() && f.isFile()) {
                int size = 176882;
                System.out.println(size);
                if(size > 0 ) {
                    FileInputStream fis = new FileInputStream(f);
                    DataInputStream dis = new DataInputStream(fis);
                    byte b[] = new byte[size];
                    dis.readFully(b);
                    System.out.println("read file succeeded");
                    fis.close();
    
                    DataOutputStream dos = new DataOutputStream(os);
                    dos.write(b);
                    System.out.println("Send file success");
                }
                else {
                    pw.println(-1);
                    pw.flush();
                }
            }
            }
        } catch (IOException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}
