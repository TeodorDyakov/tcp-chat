package bg.sofia.uni.fmi.mjt.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Scanner;

public class ChatClient {

    public static final int PORT = 54545;
    public static final String HOST = "localhost";

    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.start();
    }

    public void sendFile(String inputLine, OutputStream out) throws IOException {
        PrintWriter pw = new PrintWriter(out, true);
        String[] tokens = inputLine.split("\\s+");

        File file = new File(tokens[1]);
        long fileSz = file.length();
        pw.println(inputLine + " " + fileSz);

        byte[] bytes = new byte[16 * 1024];
        InputStream in = new FileInputStream(file);

        int count;
        while ((count = in.read(bytes)) > 0) {
            System.out.println(count);
            out.write(bytes, 0, count);
        }
        in.close();
        out.flush();
    }

    public void start() {
        try (Socket socket = new Socket(HOST, PORT);
             var in = socket.getInputStream();
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))
        ) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("To login enter: login <username> <password>" +
                "\nTo register enter register <username> <password>");

            IncomingMessagesHandler incomingMessagesHandler = new IncomingMessagesHandler(reader, in);
            incomingMessagesHandler.start();

            while (true) {
                String message = scanner.nextLine(); // read a line from the console

                if(message.startsWith("send-file")){
                    sendFile(message, socket.getOutputStream());
                }else{
                    writer.println(message); // send the message to the server
                }

                if (message.equals("quit")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("There is a problem with the network communication");
        }
    }
}
