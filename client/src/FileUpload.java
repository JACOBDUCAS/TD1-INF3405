import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class FileUpload {
    public static void main(String[] args) {
        //
        try {
            
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter ip adress : ");
            String ip = scanner.nextLine();
    
            System.out.print("Enter file path : ");
            String filePath = scanner.nextLine();
    
            Socket socket = new Socket(ip, 5001);
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
    
            Scanner sc = new Scanner(is);
            PrintWriter pw = new PrintWriter(os);
    
            String command = "READ " + filePath;
            pw.println(command);
            pw.flush();
    
    
            //String sizeStr = sc.nextLine();
    
            int sizeFile = 176882 ;//Integer.parseInt(sizeStr); //revoir cette erreur la
            if(sizeFile == -1) {
                System.out.println("File " + filePath + " not Found ");
            } else if (sizeFile == 0) {
                System.out.println("File " + filePath + " Empty ");
            } else  {
                String FileLocation = "/Users/amiratamakloe/IdeaProjects/TD1-INF3405-1/server/files/image.png";
                FileOutputStream fos = new FileOutputStream(FileLocation);
                byte b[] = new byte[10000000];
                int sum = 0;
                DataInputStream dis = new DataInputStream(is);
                System.out.println("on se rend la");
    
                while(true) {
                    int n = dis.read(b, 0, 10000000);
                    fos.write(b,0,n);
                    sum += n;
                    System.out.println(sum + " bytes downloaded");
                    if(sum >= sizeFile) break;
                }
                System.out.println("file downloaded succesfully");
                fos.close();
                dis.close();

            }
        } catch (IOException e) {
            // TODO: handle exception
            e.printStackTrace();
        }


    }
}
