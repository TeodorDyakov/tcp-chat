package bg.sofia.uni.fmi.mjt.server;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    public final int PORT = 54545;
    ServerSocket serverSocket;
    Map<String, PrintWriter> usernameToWriters = new ConcurrentHashMap<>();
    Database database;
    Map<String, OutputStream> clientsOutputStreams = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }

    public void setUpDatabase() throws IOException {
        File dbFile = new File("database.txt");
        dbFile.createNewFile();
        database = new Database(new FileReader("database.txt"), new FileWriter("database.txt", true));
        database.readDatabaseToMemory();
    }

    public void start() {
        try {
            setUpDatabase();
            int maxExecutorThreads = 128;
            ExecutorService executor = Executors.newFixedThreadPool(maxExecutorThreads);
            serverSocket = new ServerSocket(PORT);
            Socket clientSocket;
            Socket fileTransferSocket;
            while (true) {
                clientSocket = serverSocket.accept();
                fileTransferSocket = serverSocket.accept();
                ClientRequestHandler clientHandler =
                    new ClientRequestHandler(clientSocket, fileTransferSocket, usernameToWriters, database,
                        clientsOutputStreams);
                executor.execute(clientHandler);
            }
        } catch (IOException e) {
            System.out.println("There is a problem with the network communication");
        }
    }

}
