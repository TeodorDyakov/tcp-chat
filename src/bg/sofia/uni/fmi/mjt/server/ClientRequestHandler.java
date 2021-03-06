package bg.sofia.uni.fmi.mjt.server;

import bg.sofia.uni.fmi.mjt.server.exceptions.ExceptionLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientRequestHandler implements Runnable {

    static private final AtomicInteger idNum = new AtomicInteger(0);
    private final String currentGuestID;
    private final Socket socket;
    private final Socket fileTransferSocket;
    private final Map<String, PrintWriter> clientWriters;
    private final Database database;
    private final Map<String, OutputStream> clientFileTransferOutputStreams;
    ExceptionLogger logger = new ExceptionLogger("server_exceptions.txt");
    private String loggedInUser;

    ClientRequestHandler(Socket socket, Socket fileTransferSocket, Map<String, PrintWriter> clientWriters,
                         Database database,
                         Map<String, OutputStream> clientFileTransferOutputStreams) {
        this.socket = socket;
        this.fileTransferSocket = fileTransferSocket;
        this.clientWriters = clientWriters;
        this.database = database;
        this.clientFileTransferOutputStreams = clientFileTransferOutputStreams;
        this.currentGuestID = "guest" + idNum.incrementAndGet();
    }

    String getLoggedInUser() {
        return loggedInUser;
    }

    Set<String> getLoggedInUsers() {
        return new HashSet<>(clientWriters.keySet());
    }

    Database getDatabase() {
        return database;
    }

    boolean isUserOnline(String user) {
        return clientWriters.containsKey(user);
    }

    void loginUser(String username) {
        loggedInUser = username;
        synchronized (clientWriters) {
            var loggedInUserWriter = clientWriters.get(currentGuestID);
            clientWriters.put(username, loggedInUserWriter);
            clientWriters.remove(currentGuestID);
        }
        synchronized (clientFileTransferOutputStreams) {
            var fileTransferOut = clientFileTransferOutputStreams.get(currentGuestID);
            clientFileTransferOutputStreams.put(username, fileTransferOut);
            clientFileTransferOutputStreams.remove(currentGuestID);
        }
    }

    String getCurrentGuestID() {
        return currentGuestID;
    }

    void sendLineToClient(String msg, PrintWriter to) {
        synchronized (to) {
            to.println(msg);
        }
    }

    void handleRequest(String inputLine) {
        CommandExecutor cmdExec = new CommandExecutor(this);
        Command command = CommandCreator.newCommand(inputLine);
        var commandResult = cmdExec.execute(command);
        for (var e : commandResult.entrySet()) {
            sendLineToClient(e.getValue(), clientWriters.get(e.getKey()));
        }
    }

    @Override
    public void run() {
        try (var fileTransferOut = fileTransferSocket.getOutputStream();
             var fileTransferIn = fileTransferSocket.getInputStream();
             var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             var writer = new PrintWriter(socket.getOutputStream(), true)) {
            clientWriters.put(currentGuestID, writer);
            clientFileTransferOutputStreams.put(currentGuestID, fileTransferOut);
            String inputLine;
            while ((inputLine = reader.readLine()) != null) { // read the message from the client
                System.out.println("Request received:" + inputLine);
                if (inputLine.startsWith("send-file-to")) {
                    final String line = inputLine;
                    new Thread(() -> transferFile(fileTransferIn, line)).start();
                } else {
                    handleRequest(inputLine);
                }
            }
        } catch (IOException exception) {
            logger.writeExceptionToFile(exception);
        } finally {
            try {
                socket.close();
            } catch (IOException exception) {
                logger.writeExceptionToFile(exception);
            }
            try {
                fileTransferSocket.close();
            } catch (IOException exception) {
                logger.writeExceptionToFile(exception);
            }
        }
    }

    void transferFile(InputStream in, String inputLine) {
        if (loggedInUser == null) {
            sendLineToClient(ServerResponse.NOT_LOGGED_IN, clientWriters.get(currentGuestID));
        }
        final var tokens = inputLine.split("\\s+");
        if (tokens.length < 4) {
            sendLineToClient(ServerResponse.INVALID_COMMAND, clientWriters.get(loggedInUser));
        }
        final var user = tokens[1];
        final var fileSz = Integer.parseInt(tokens[3]);

        final var bytes = new byte[8192];
        int bytesLeftToRead = fileSz;
        int count;

        var printWriter = clientWriters.get(user);
        var out = clientFileTransferOutputStreams.get(user);
        if (out == null) {
            sendLineToClient("[ no user with name " + user + " online ]", clientWriters.get(loggedInUser));
            return;
        }
        sendLineToClient(inputLine, printWriter);
        synchronized (out) {
            try {
                while ((count = in.read(bytes, 0, Math.min(bytes.length, bytesLeftToRead))) > 0) {
                    out.write(bytes, 0, count);
                    bytesLeftToRead -= count;
                }
                out.flush();
                sendLineToClient(ServerResponse.FILE_SENT_SUCCESSFULLY, clientWriters.get(loggedInUser));
            } catch (IOException e) {
                sendLineToClient(ServerResponse.FILE_TRANSFER_FAILED, clientWriters.get(loggedInUser));
                logger.writeExceptionToFile(e);
            }
        }
    }

}
