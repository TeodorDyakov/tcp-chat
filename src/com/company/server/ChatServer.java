package com.company.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    public final int PORT = 54545;
    ServerSocket serverSocket;
    Set<PrintWriter> clientWriters = ConcurrentHashMap.newKeySet();
    Map<String, String> usernameToPassword = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }

    public void start() {
        try {
            int maxExecutorThreads = 128;
            ExecutorService executor = Executors.newFixedThreadPool(maxExecutorThreads);
            serverSocket = new ServerSocket(PORT);
            Socket clientSocket;
            while (true) {
                clientSocket = serverSocket.accept();
                ClientRequestHandler clientHandler =
                    new ClientRequestHandler(clientSocket, clientWriters, usernameToPassword);
                executor.execute(clientHandler);
            }
        } catch (IOException e) {
            System.out.println("There is a problem with the network communication");
        }
    }

}
