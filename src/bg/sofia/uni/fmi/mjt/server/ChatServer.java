package bg.sofia.uni.fmi.mjt.server;

import bg.sofia.uni.fmi.mjt.server.exceptions.CouldNotSetUpDatabaseException;
import bg.sofia.uni.fmi.mjt.server.exceptions.ExceptionLogger;
import bg.sofia.uni.fmi.mjt.server.exceptions.ServerCreationException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    static ExceptionLogger logger = new ExceptionLogger("server_exceptions.txt");
    private final int PORT = 54545;
    private final Map<String, PrintWriter> usernameToWriters = new HashMap<>();
    private final Map<String, OutputStream> clientsOutputStreams = new HashMap<>();
    private Database database;

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        try {
            server.start();
        } catch (CouldNotSetUpDatabaseException | ServerCreationException e) {
            logger.writeExceptionToFile(e);
        }
    }

    public void setUpDatabase() throws IOException {
        File dbFile = new File("database.txt");
        dbFile.createNewFile();
        database = new Database(new FileReader("database.txt"), new FileWriter("database.txt", true));
        database.readDatabaseToMemory();
    }

    public void start() throws CouldNotSetUpDatabaseException, ServerCreationException {
        try {
            setUpDatabase();
        } catch (IOException exception) {
            throw new CouldNotSetUpDatabaseException("could not set up database!", exception);
        }
        int maxExecutorThreads = 128;
        ExecutorService executor = Executors.newFixedThreadPool(maxExecutorThreads);
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException exception) {
            throw new ServerCreationException("could not create server!", exception);
        }

        Socket clientSocket;
        Socket fileTransferSocket;
        while (true) {
            try {
                clientSocket = serverSocket.accept();
                fileTransferSocket = serverSocket.accept();
                ClientRequestHandler clientHandler =
                    new ClientRequestHandler(clientSocket, fileTransferSocket, usernameToWriters, database,
                        clientsOutputStreams);
                executor.execute(clientHandler);
            } catch (IOException exception) {
                logger.writeExceptionToFile(exception);
            }
        }
    }

}
