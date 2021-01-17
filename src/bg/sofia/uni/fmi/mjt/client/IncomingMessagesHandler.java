package bg.sofia.uni.fmi.mjt.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

public class IncomingMessagesHandler extends Thread {

    BufferedReader reader;
    InputStream in;

    IncomingMessagesHandler(BufferedReader reader, InputStream in) {
        this.reader = reader;
        this.in = in;
    }

    @Override
    public void run() {
        String inputLine;
        while (true) {
            try {
                if ((inputLine = reader.readLine()) == null) {
                    ConsoleOutput.printLineToConsole("could not read from server");
                    break;
                }
            } catch (IOException exception) {
                ConsoleOutput.printLineToConsole("could not read from server");
                break;
            }
            if (inputLine.startsWith("send-file")) {
                Thread fileReceiveHandler = new FileReceiveHandler(inputLine, in);
                fileReceiveHandler.start();
            } else {
                ConsoleOutput.printLineToConsole(inputLine);
            }
        }
    }
}

