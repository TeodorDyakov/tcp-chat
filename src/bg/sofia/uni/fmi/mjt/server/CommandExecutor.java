package bg.sofia.uni.fmi.mjt.server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class CommandExecutor {
    static final String SEND_MESSAGE_TO = "send-msg-to";
    static final String REGISTER = "register";
    static final String LOGIN = "login";
    static final String SEND_MESSAGE = "send-msg";
    ClientRequestHandler clientRequestHandler;

    CommandExecutor(ClientRequestHandler clientRequestHandler) {
        this.clientRequestHandler = clientRequestHandler;
    }

    public Map<String, String> execute(Command cmd) {
        return switch (cmd.command()) {
            case SEND_MESSAGE -> message(cmd.arguments());
            case LOGIN -> login(cmd.arguments());
            case REGISTER -> register(cmd.arguments());
            case SEND_MESSAGE_TO -> messageTo(cmd.arguments());
            default -> invalidCommand();
        };
    }

    Map<String, String>invalidCommand(){
        var invalid = new HashMap<String, String>();
        if(clientRequestHandler.getLoggedInUser() == null){
            invalid.put(clientRequestHandler.getCurrentGuestID(), ServerResponse.INVALID_COMMAND);
        }else{
            invalid.put(clientRequestHandler.getLoggedInUser(), ServerResponse.INVALID_COMMAND);
        }
        return invalid;
    }

    Map<String, String> messageTo(String[] arguments) {
        Map<String, String> result = new HashMap<>();
        if (clientRequestHandler.getLoggedInUser() == null) {
            result.put(clientRequestHandler.getCurrentGuestID(), ServerResponse.NOT_LOGGED_IN);
            return result;
        }
        if (arguments.length < 2) {
            result.put(clientRequestHandler.getLoggedInUser(), ServerResponse.NOT_ENOUGH_ARGUMENTS);
        }
        String toUsername = arguments[0];
        String message = arguments[1];
        if (!clientRequestHandler.isUserOnline(toUsername)) {
            result.put(clientRequestHandler.getLoggedInUser(), ServerResponse.NO_USER_WITH_THIS_NAME_ONLINE);
            return result;
        }
        message = formatMessage(message);
        result.put(clientRequestHandler.getLoggedInUser(), "(private message to " + toUsername + ")" + message);
        result.put(toUsername, "(private message)" + message);
        return result;
    }


    Map<String, String> message(String[] arguments) {
        Map<String, String> result = new HashMap<>();
        if (clientRequestHandler.getLoggedInUser() == null) {
            result.put(clientRequestHandler.getCurrentGuestID(), ServerResponse.NOT_LOGGED_IN);
            return result;
        }
        if (arguments.length == 0) {
            result.put(clientRequestHandler.getLoggedInUser(), ServerResponse.NOT_ENOUGH_ARGUMENTS);
            return result;
        }
        for (String user : clientRequestHandler.getLoggedInUsers()) {
            result.put(user, formatMessage(arguments[0]));
        }
        return result;
    }

    Map<String, String> login(String[] arguments) {
        var result = new HashMap<String, String>();

        if (arguments.length < 2) {
            result.put(clientRequestHandler.getCurrentGuestID(), ServerResponse.NOT_ENOUGH_ARGUMENTS);
            return result;
        }

        String username = arguments[0];
        String password = arguments[1];

        Database db = clientRequestHandler.getDatabase();

        if (db.containsUser(username) && db.getPassOfUser(username).equals(password)) {
            clientRequestHandler.loginUser(username);
            for (String user : clientRequestHandler.getLoggedInUsers()) {
                result.put(user, username + " has joined the chat");
            }
            result.put(username, ServerResponse.LOGGED_IN + System.lineSeparator()
                + username + " has joined the chat");
            return result;
        }
        result.put(clientRequestHandler.getCurrentGuestID(), ServerResponse.INVALID_USERNAME_OR_PASS);
        return result;
    }

    String formatMessage(String message) {
        URLshortener urLshortener = new URLshortener();
        message = urLshortener.shortenURLs(message);
        return "%s %s:%s"
            .formatted(clientRequestHandler.getLoggedInUser(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                message);
    }

    Map<String, String> register(String[] arguments) {
        var result = new HashMap<String, String>();

        if (arguments.length < 2) {
            result.put(clientRequestHandler.getCurrentGuestID(), ServerResponse.NOT_ENOUGH_ARGUMENTS);
            return result;
        }

        String username = arguments[0];
        String password = arguments[1];

        Database db = clientRequestHandler.getDatabase();

        if (db.containsUser(username)) {
            result.put(clientRequestHandler.getCurrentGuestID(), ServerResponse.USERNAME_TAKEN);
            return result;
        }

        clientRequestHandler.loginUser(username);
        db.savePassAndName(username, password);
        for (String user : clientRequestHandler.getLoggedInUsers()) {
            result.put(user, username + " has joined the chat");
        }
        result.put(username, ServerResponse.REGISTERED + System.lineSeparator()
            + username + " has joined the chat");
        return result;
    }
}
