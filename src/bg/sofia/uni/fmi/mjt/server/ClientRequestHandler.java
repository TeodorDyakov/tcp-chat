package bg.sofia.uni.fmi.mjt.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ClientRequestHandler implements Runnable {

    static final String SEND_MESSAGE_TO = "send-msg-to";
    static final String REGISTER = "register";
    static final String LOGIN = "login";
    static final String SEND_MESSAGE = "send-msg";

    private final Socket socket;
    private final Socket fileTransferSocket;

    private final Map<String, PrintWriter> clientWriters;
    private final Database database;
    private final Map<String, OutputStream> clientOutputStreams;
    private String loggedInUser;

    ClientRequestHandler(Socket socket, Socket fileTransferSocket, Map<String, PrintWriter> clientWriters,
                         Database database,
                         Map<String, OutputStream> clientOutputStreams) {
        this.socket = socket;
        this.fileTransferSocket = fileTransferSocket;
        this.clientWriters = clientWriters;
        this.database = database;
        this.clientOutputStreams = clientOutputStreams;
    }

    public void execute(Command cmd, PrintWriter writer, OutputStream outputStream) {
        switch (cmd.command()) {
            case SEND_MESSAGE -> message(cmd.arguments(), writer);
            case LOGIN -> login(cmd.arguments(), writer, outputStream);
            case REGISTER -> register(cmd.arguments(), writer, outputStream);
            case SEND_MESSAGE_TO -> messageTo(cmd.arguments(), writer);
            default -> sendLineToClient(ServerResponse.INVALID_COMMAND, writer);
        }
    }

    synchronized void register(String[] arguments, PrintWriter writer, OutputStream outputStream) {
        String username = arguments[0];
        String password = arguments[1];
        if (database.containsUser(username)) {
            sendLineToClient(ServerResponse.USERNAME_TAKEN, writer);
            return;
        }
        loggedInUser = username;
        database.savePassAndName(username, password);

        sendLineToClient(ServerResponse.REGISTERED, writer);

        clientWriters.put(loggedInUser, writer);
        clientOutputStreams.put(loggedInUser, outputStream);

        broadcastMessage(loggedInUser + " has joined the chat");
    }

    synchronized void sendLineToClient(String msg, PrintWriter to) {
        to.println(msg);
    }

    void messageTo(String[] arguments, PrintWriter out) {
        if (loggedInUser == null) {
            sendLineToClient(ServerResponse.NOT_LOGGED_IN, out);
            return;
        }
        String toUsername = arguments[0];
        String message = arguments[1];
        var writer = clientWriters.get(toUsername);

        if (writer != null) {
            message = formatMessage(message);
            sendLineToClient("(private message to " + toUsername + ")" + message, out);
            sendLineToClient("(private message)" + message, writer);
        } else {
            sendLineToClient("[ no user with this name online ]", out);
        }
    }

    void message(String[] arguments, PrintWriter out) {
        if (loggedInUser == null) {
            sendLineToClient(ServerResponse.NOT_LOGGED_IN, out);
            return;
        }
        String message = formatMessage(arguments[0]);
        broadcastMessage(message);
    }

    void broadcastMessage(String msg) {
        for (var pw : clientWriters.values()) {
            sendLineToClient(msg, pw);
        }
    }

    private void login(String[] arguments, PrintWriter out, OutputStream outputStream) {
        String username = arguments[0];
        String password = arguments[1];
        if (database.containsUser(username) && database.getPassOfUser(username).equals(password)) {
            loggedInUser = username;
            clientWriters.put(loggedInUser, out);
            clientOutputStreams.put(loggedInUser, outputStream);
            sendLineToClient(ServerResponse.LOGGED_IN, out);
            broadcastMessage(loggedInUser + " has joined the chat");
            return;
        }
        sendLineToClient(ServerResponse.INVALID_USERNAME_OR_PASS, out);
    }

    public String formatMessage(String message) {
        URLshortener urLshortener = new URLshortener();
        message = urLshortener.shortenURLs(message);
        return "%s %s:%s"
            .formatted(loggedInUser, LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                message);
    }

    public void handleRequest(String inputLine, Writer writer, OutputStream out) {
        Command command = CommandCreator.newCommand(inputLine);
        execute(command, new PrintWriter(writer, true), out);
    }

    public synchronized void transferFile(InputStream in, String inputLine) throws IOException {
        String[] tokens = inputLine.split("\\s+");

        String user = tokens[1];
        int fileSz = Integer.parseInt(tokens[3]);

        byte[] bytes = new byte[8192];
        int bytesRead = 0;
        int count;

        var printWriter = clientWriters.get(user);
        var out = clientOutputStreams.get(user);
        if (out == null) {
            return;
        }
        sendLineToClient(inputLine, printWriter);

        while (bytesRead < fileSz && (count = in.read(bytes)) > 0) {
            out.write(bytes, 0, count);
            bytesRead += count;
        }
        out.flush();
        sendLineToClient("[ file received from " + loggedInUser + "]", printWriter);
        sendLineToClient(ServerResponse.FILE_SENT_SUCCESSFULLY, clientWriters.get(loggedInUser));
    }

    @Override
    public void run() {
        try (var fileTransferOut = fileTransferSocket.getOutputStream();
             var fileTransferIn = fileTransferSocket.getInputStream();
             var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             var writer = new PrintWriter(socket.getOutputStream(), true)) {

            String inputLine;
            while ((inputLine = reader.readLine()) != null) { // read the message from the client
                System.out.println("Request received:" + inputLine);
                if (inputLine.startsWith("send-file")) {
                    transferFile(fileTransferIn, inputLine);
                } else
                    handleRequest(inputLine, writer, fileTransferOut);
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
