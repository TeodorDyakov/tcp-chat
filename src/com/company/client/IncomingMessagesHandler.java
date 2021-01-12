package com.company.client;

import java.io.BufferedReader;
import java.io.IOException;

public class IncomingMessagesHandler extends Thread {

    BufferedReader reader;

    IncomingMessagesHandler(BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    public void run() {
        String inputLine;
        while (true) {
            try {
                if ((inputLine = reader.readLine()) == null) {
                    break;
                }
            } catch (IOException exception) {
                System.out.println(exception.getMessage());
                break;
            }
            System.out.println(inputLine);
        }
    }
}

