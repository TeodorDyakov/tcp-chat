package bg.sofia.uni.fmi.mjt.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ClientRequestHandler implements Runnable {

    private final Socket socket;
    private final Map<String, PrintWriter> clientWriters;
    private final Database database;
    private String loggedInUsername;

    ClientRequestHandler(Socket socket, Map<String, PrintWriter> clientWriters, Database database) {
        this.socket = socket;
        this.clientWriters = clientWriters;
        this.database = database;
    }

    synchronized void broadcastMessage(String message) {
        for (PrintWriter printWriter : clientWriters.values()) {
            printWriter.println(message);
        }
    }

    boolean isLoggedIn() {
        return loggedInUsername != null;
    }

    synchronized boolean register(String username, String password) {
        if (database.containsUser(username)) {
            return false;
        }
        database.savePassAndName(username, password);
        loggedInUsername = username;
        return true;
    }

    void handleMessage(String request) {
        request = request.substring("send".length() + 1);
        String[] tokens = request.split(" ");
        if (tokens.length >= 2) {
            if (tokens[0].equals("to")) {
                String toUsername = tokens[1];
                String message = request.substring(3 + toUsername.length());
                PrintWriter writer = clientWriters.get(toUsername);
                if (writer != null) {
                    message = formatMessage(message).trim();
                    writer.println("(private message)" + message);
                    clientWriters.get(loggedInUsername).println("(private message to " + toUsername + ")" + message);
                } else {
                    clientWriters.get(loggedInUsername).println("[ No such user ]");
                }
                return;
            }
        }
        String message = formatMessage(request);
        broadcastMessage(message);
    }

    public String formatMessage(String message) {
        return "%s %s:%s"
            .formatted(loggedInUsername, LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                message);
    }

    private boolean login(String user, String pass) {
        if (database.containsUser(user) && database.getPassOfUser(user).equals(pass)) {
            loggedInUsername = user;
            return true;
        }
        return false;
    }

    public void handleRequest(String inputLine, Writer writer) {
        var out = new PrintWriter(writer, true);
        String[] tokens = inputLine.split(" ");

        if (tokens.length == 0) {
            out.println(ServerResponse.INVALID_COMMAND);
        } else if (inputLine.startsWith("send")) {
            if (!isLoggedIn()) {
                out.println(ServerResponse.NOT_LOGGED_IN);
            } else {
                handleMessage(inputLine);
            }
        } else if (tokens[0].equals("register") && tokens.length == 3) {
            if (register(tokens[1], tokens[2])) {
                out.println(ServerResponse.REGISTERED);
                clientWriters.put(loggedInUsername, out);
                broadcastMessage(loggedInUsername + " has joined the chat");
            } else {
                out.println(ServerResponse.USERNAME_TAKEN);
            }
        } else if (tokens[0].equals("login") && tokens.length == 3) {
            if (login(tokens[1], tokens[2])) {
                out.println(ServerResponse.LOGGED_IN);
                clientWriters.put(loggedInUsername, out);
                broadcastMessage(loggedInUsername + " has joined the chat");
            } else {
                out.println(ServerResponse.INVALID_USERNAME_OR_PASS);
            }
        } else if (tokens[0].equals("quit") && tokens.length == 1) {
            out.println(ServerResponse.DISCONNECTED);
            if (loggedInUsername != null) {
                clientWriters.remove(loggedInUsername);
            }
        } else {
            out.println(ServerResponse.INVALID_COMMAND);
        }
    }

    @Override
    public void run() {
        try (var out = new PrintWriter(socket.getOutputStream(), true);
             var in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) { // read the message from the client
                System.out.println("Request received:" + inputLine);
                handleRequest(inputLine, out);
            }
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException exception) {
                System.out.println(exception.getMessage());
            }
        }
    }
}
