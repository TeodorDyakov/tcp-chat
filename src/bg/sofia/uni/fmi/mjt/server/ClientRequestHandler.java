package bg.sofia.uni.fmi.mjt.server;

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

public class ClientRequestHandler implements Runnable {

    static private int idNum = 0;
    private final String currentGuestID;
    private final Socket socket;
    private final Socket fileTransferSocket;
    private final Map<String, PrintWriter> clientWriters;
    private final Database database;
    private final Map<String, OutputStream> clientFileTransferOutputStreams;
    private String loggedInUser;

    ClientRequestHandler(Socket socket, Socket fileTransferSocket, Map<String, PrintWriter> clientWriters,
                         Database database,
                         Map<String, OutputStream> clientFileTransferOutputStreams) {
        this.socket = socket;
        this.fileTransferSocket = fileTransferSocket;
        this.clientWriters = clientWriters;
        this.database = database;
        this.clientFileTransferOutputStreams = clientFileTransferOutputStreams;
        idNum++;
        this.currentGuestID = "guest" + idNum;
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

    synchronized void loginUser(String username) {
        loggedInUser = username;
        var loggedInUserWriter = clientWriters.get(currentGuestID);
        clientWriters.put(username, loggedInUserWriter);
        clientWriters.remove(currentGuestID);
        var fileTransferOut = clientFileTransferOutputStreams.get(currentGuestID);
        clientFileTransferOutputStreams.put(username, fileTransferOut);
        clientFileTransferOutputStreams.remove(currentGuestID);
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
                if (inputLine.startsWith("send-file")) {
                    final String line = inputLine;
                    new Thread(() -> transferFile(fileTransferIn, line)).start();
                } else {
                    handleRequest(inputLine);
                }
            }
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            try {
                fileTransferSocket.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    synchronized void transferFile(InputStream in, String inputLine) {
        if (loggedInUser == null) {
            sendLineToClient(ServerResponse.NOT_LOGGED_IN, clientWriters.get(currentGuestID));
        }
        String[] tokens = inputLine.split("\\s+");
        if (tokens.length < 4) {
            sendLineToClient(ServerResponse.INVALID_COMMAND, clientWriters.get(loggedInUser));
        }
        String user = tokens[1];
        int fileSz = Integer.parseInt(tokens[3]);

        byte[] bytes = new byte[8192];
        int bytesRead = 0;
        int count;

        var printWriter = clientWriters.get(user);
        var out = clientFileTransferOutputStreams.get(user);
        if (out == null) {
            sendLineToClient("[ no user with name " + user + " online ]", clientWriters.get(loggedInUser));
            return;
        }
        sendLineToClient(inputLine, printWriter);
        try {
            while (bytesRead < fileSz && (count = in.read(bytes)) > 0) {
                out.write(bytes, 0, count);
                bytesRead += count;
            }
            out.flush();
            sendLineToClient(ServerResponse.FILE_SENT_SUCCESSFULLY, clientWriters.get(loggedInUser));
        } catch (IOException e) {
            sendLineToClient(ServerResponse.FILE_TRANSFER_FAILED, clientWriters.get(loggedInUser));
            System.out.println(e.getMessage());
        }
    }

}
