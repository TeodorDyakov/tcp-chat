package com.company.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

public class ClientRequestHandler implements Runnable {

    Socket socket;
    Set<PrintWriter> clientWriters;
    Map<String, String> userToPassword;
    String loggedInUsername;

    ClientRequestHandler(Socket socket, Set<PrintWriter> clientWriters, Map<String, String> userToPassword) {
        this.socket = socket;
        this.clientWriters = clientWriters;
        this.userToPassword = userToPassword;
    }

    synchronized void broadcastMessage(String message) {
        for (PrintWriter printWriter : clientWriters) {
                printWriter.println(message);
        }
    }

    synchronized public String register(String username, String password) {
        if (userToPassword.containsKey(username)) {
            return "[ username already taken ]";
        }
        userToPassword.put(username, password);
        loggedInUsername = username;
        return "[ registered successfully ]";
    }

    public String handleRequest(String request) throws IOException {
        String[] tokens = request.split("\\s+");
        if (tokens.length == 0) {
            return "[ invalid command ]";
        }
        if (tokens[0].equals("register") && tokens.length == 3) {
            return register(tokens[1], tokens[2]);
        }
        if (tokens[0].equals("login") && tokens.length == 3) {
            return login(tokens[1], tokens[2]);
        }
        if (tokens[0].equals("quit") && tokens.length == 1) {
            return "[ disconnected ]";
        }
        return "[ invalid command ]";
    }

    void handleMessage(String request){
        if(loggedInUsername == null){
            return;
        }
        String message = formatMessage(request);
        broadcastMessage(message);
    }

    public String formatMessage(String message) {
        message = message.substring(4).trim();
        return "%s %s: %s"
            .formatted(loggedInUsername, LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                message);
    }

    private String login(String user, String pass) {
        if (userToPassword.containsKey(user) && userToPassword.get(user).equals(pass)) {
            loggedInUsername = user;
            return "[ logged in ]";
        }
        return "[ invalid username/password combination ]";
    }

    @Override
    public void run() {
        try (var out = new PrintWriter(socket.getOutputStream(), true);
             var in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            clientWriters.add(out);
            String inputLine;
            while ((inputLine = in.readLine()) != null) { // read the message from the client
                System.out.println("Request received:" + inputLine);

                String response = handleRequest(inputLine);
                if(inputLine.startsWith("send")){
                    handleMessage(inputLine);
                    continue;
                }
                out.println(response);
                if(response.equals(ServerResponse.DISCONNECTED)){
                    socket.close();
                }else if(response.equals(ServerResponse.REGISTERED) || response.equals(ServerResponse.LOGGED_IN)){
                    broadcastMessage(loggedInUsername + " has joined the chat");
                }
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
