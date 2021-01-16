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

    ClientRequestHandler(Socket socket, Socket fileTransferSocket, Map<String, PrintWriter> clientWriters, Database database,
                         Map<String, OutputStream> clientOutputStreams) {
        this.socket = socket;
        this.fileTransferSocket = fileTransferSocket;
        this.clientWriters = clientWriters;
        this.database = database;
        this.clientOutputStreams = clientOutputStreams;
    }

    public void execute(Command cmd, PrintWriter out, OutputStream outputStream) {
        switch (cmd.command()) {
            case SEND_MESSAGE -> message(cmd.arguments(), out);
            case LOGIN -> login(cmd.arguments(), out, outputStream);
            case REGISTER -> register(cmd.arguments(), out, outputStream);
            case SEND_MESSAGE_TO -> messageTo(cmd.arguments(), out);
            default -> out.println(ServerResponse.INVALID_COMMAND);
        }
    }

    synchronized void register(String[] arguments, PrintWriter out, OutputStream outputStream) {
        String username = arguments[0];
        String password = arguments[1];
        if (database.containsUser(username)) {
            out.println(ServerResponse.USERNAME_TAKEN);
            return;
        }
        loggedInUser = username;
        database.savePassAndName(username, password);

        out.println(ServerResponse.REGISTERED);

        clientWriters.put(loggedInUser, out);
        clientOutputStreams.put(loggedInUser, outputStream);

        broadcastMessage(loggedInUser + " has joined the chat");
    }

    synchronized void messageTo(String[] arguments, PrintWriter out) {
        if (loggedInUser == null) {
            out.println(ServerResponse.NOT_LOGGED_IN);
            return;
        }
        String toUsername = arguments[0];
        String message = arguments[1];
        var writer = clientWriters.get(toUsername);

        if (writer != null) {
            message = formatMessage(message);
            out.println("(private message to " + toUsername + ")" + message);
            writer.println("(private message)" + message);
        } else {
            out.println("[ no user with this name online ]");
        }
    }

    synchronized void message(String[] arguments, PrintWriter out) {
        if (loggedInUser == null) {
            out.println(ServerResponse.NOT_LOGGED_IN);
            return;
        }
        String message = formatMessage(arguments[0]);
        broadcastMessage(message);
    }

    synchronized void broadcastMessage(String msg) {
        for (var pw : clientWriters.values()) {
            pw.println(msg);
        }
    }

    private void login(String[] arguments, PrintWriter out, OutputStream outputStream) {
        String username = arguments[0];
        String password = arguments[1];
        if (database.containsUser(username) && database.getPassOfUser(username).equals(password)) {
            loggedInUser = username;
            clientWriters.put(loggedInUser, out);
            clientOutputStreams.put(loggedInUser, outputStream);
            out.println(ServerResponse.LOGGED_IN);
            broadcastMessage(loggedInUser + " has joined the chat");
            return;
        }
        out.println(ServerResponse.INVALID_USERNAME_OR_PASS);
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
        printWriter.println(inputLine);

        while (bytesRead < fileSz && (count = in.read(bytes)) > 0) {
            out.write(bytes, 0, count);
            bytesRead += count;
        }
        out.flush();
        printWriter.printf("[ file received from %s ]\n", loggedInUser);
        clientWriters.get(loggedInUser).println(ServerResponse.FILE_SENT_SUCCESSFULLY);
    }

    @Override
    public void run() {
        try (var fileTransferOut = fileTransferSocket.getOutputStream();
             var fileTransferIn = fileTransferSocket.getInputStream();
             var out = socket.getOutputStream();
             var in = socket.getInputStream()) {
            var reader = new BufferedReader(new InputStreamReader(in));
            var writer = new PrintWriter(out, true);
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
