package com.company.client;

import java.io.BufferedReader;
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
        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("To login enter: login <username> <password>" +
                "\nTo register enter register <username> <password>");
            boolean loggedIn = false;

            while (!loggedIn) {
                writer.println(scanner.nextLine());
                String serverResponse = null;
                try {
                    serverResponse = reader.readLine();
                    System.out.println(serverResponse);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
                assert serverResponse != null;
                if (serverResponse.equals(ServerResponse.REGISTERED) ||
                    serverResponse.equals(ServerResponse.LOGGED_IN)) {
                    loggedIn = true;
                }
            }
            IncomingMessagesHandler incomingMessagesHandler = new IncomingMessagesHandler(reader);
            incomingMessagesHandler.start();
            while (true) {
                String message = scanner.nextLine(); // read a line from the console
                if (message.equals("quit")) {
                    break;
                }
                writer.println("send " + message); // send the message to the server
            }
        } catch (IOException e) {
            System.out.println("There is a problem with the network communication");
        }
    }
}
