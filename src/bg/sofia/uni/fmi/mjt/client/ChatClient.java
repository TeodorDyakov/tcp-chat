package bg.sofia.uni.fmi.mjt.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {

    public static final int PORT = 54545;
    public static final String HOST = "localhost";

    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.start();
    }

    public void start() {
        new File("received_files").mkdir();
        try (Socket socket = new Socket(HOST, PORT);
             Socket fileTransferSocket = new Socket(HOST, PORT);
             var in = socket.getInputStream();
             var fileTransferSocketInputStream = fileTransferSocket.getInputStream()) {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            Scanner scanner = new Scanner(System.in);
            System.out.println("To login enter: login <username> <password>" +
                "\nTo register enter register <username> <password>");

            IncomingMessagesHandler incomingMessagesHandler =
                new IncomingMessagesHandler(reader, fileTransferSocketInputStream);
            incomingMessagesHandler.start();

            while (true) {
                String message = scanner.nextLine(); // read a line from the console

                if (message.startsWith("send-file")) {
                    Thread fileSendHandler = new FileSendHandler(message, writer, fileTransferSocket.getOutputStream());
                    fileSendHandler.start();
                } else {
                    MessageSender.sendMessage(writer, message); // send the message to the server
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
