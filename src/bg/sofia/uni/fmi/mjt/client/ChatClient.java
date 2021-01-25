package bg.sofia.uni.fmi.mjt.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {

    private final int PORT = 54545;
    private final String HOST = "localhost";
    private final ConsolePrinter consolePrinter = new ConsolePrinter();

    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.start();
    }

    void start() {
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
                new IncomingMessagesHandler(reader, fileTransferSocketInputStream, consolePrinter);
            incomingMessagesHandler.start();
            MessageSender messageSender = new MessageSender(writer);
            while (true) {
                String message = scanner.nextLine(); // read a line from the console

                if (message.startsWith("send-file")) {
                    Thread fileSendHandler =
                        new FileSendHandler(message, messageSender, fileTransferSocket.getOutputStream(),
                            consolePrinter);
                    fileSendHandler.start();
                } else {
                    messageSender.sendMessage(message); // send the message to the server
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
