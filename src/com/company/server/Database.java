package com.company.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Database {

    private final Map<String, String>userToPassword = new HashMap<>();

    Database(){
        try {
            File database = new File("database.txt");
            database.createNewFile();
            readDatabaseToMemory();
        } catch (IOException e) {
            System.out.println("An error occurred.");
        }
    }

    public String getPassOfUser(String user){
        return userToPassword.get(user);
    }

    public boolean containsUser(String user){
        return userToPassword.containsKey(user);
    }

    private void readDatabaseToMemory(){
        try {
            Scanner scanner = new Scanner(new File("database.txt"));
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                String[] tokens = line.split("\\s+");
                if(tokens.length == 2) {
                    userToPassword.put(tokens[0], tokens[1]);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void savePassAndName(String username, String pass){
        userToPassword.put(username, pass);
        FileWriter myWriter = null;
        try {
            myWriter = new FileWriter("database.txt");
            myWriter.write(username + ":" + pass);
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

    public static void main(String[] args) {
        Database db = new Database();
        db.savePassAndName("tedy", "123");
        System.out.println(db.getPassOfUser("tedy"));
    }
}
