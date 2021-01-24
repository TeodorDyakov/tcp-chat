package bg.sofia.uni.fmi.mjt.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class Database {

    private final Map<String, String> userToPassword = new HashMap<>();
    private final Reader reader;
    private final Writer writer;

    Database(Reader reader, Writer writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public String getPassOfUser(String user) {
        return userToPassword.get(user);
    }

    public boolean containsUser(String user) {
        return userToPassword.containsKey(user);
    }

    public synchronized void readDatabaseToMemory() {
        try {
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line = bufferedReader.readLine();
            while (line != null) {
                String[] tokens = line.split(":");
                if (tokens.length == 2) {
                    userToPassword.put(tokens[0], tokens[1]);
                }
                line = bufferedReader.readLine();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public synchronized void savePassAndName(String username, String pass) {
        userToPassword.put(username, pass);
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        try {
            bufferedWriter.write(username + ":" + pass);
            bufferedWriter.write(System.lineSeparator());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        try {
            bufferedWriter.flush();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
