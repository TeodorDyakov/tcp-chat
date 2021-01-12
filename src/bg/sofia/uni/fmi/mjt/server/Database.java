package bg.sofia.uni.fmi.mjt.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class Database {

    private final Map<String, String> userToPassword = new HashMap<>();

    Database() {
        try {
            String FILE_NAME = "database.txt";
            File database = new File(FILE_NAME);
            database.createNewFile();
        } catch (IOException e) {
            System.out.println("An error occurred.");
        }
    }

    public static void main(String[] args) {
        Database db = new Database();
        db.savePassAndName("tedy", "123");
        System.out.println(db.getPassOfUser("tedy"));
        System.out.println(db.containsUser("tedy"));
    }

    public String getPassOfUser(String user) {
        return userToPassword.get(user);
    }

    public boolean containsUser(String user) {
        return userToPassword.containsKey(user);
    }

    public void readDatabaseToMemory(Reader reader) {
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

    public void savePassAndName(String username, String pass) {
        userToPassword.put(username, pass);
        FileWriter myWriter = null;
        try {
            myWriter = new FileWriter("database.txt", true);
            myWriter.write(username + ":" + pass);
            myWriter.write(System.lineSeparator());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        try {
            if (myWriter != null) {
                myWriter.close();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
