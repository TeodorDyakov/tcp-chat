package bg.sofia.uni.fmi.mjt.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

public class IncomingMessagesHandler extends Thread {

    private final BufferedReader reader;
    private final InputStream in;
    private final ConsolePrinter consolePrinter;

    IncomingMessagesHandler(BufferedReader reader, InputStream in, ConsolePrinter consolePrinter) {
        this.reader = reader;
        this.in = in;
        this.consolePrinter = consolePrinter;
    }

    @Override
    public void run() {
        String inputLine;
        while (true) {
            try {
                if ((inputLine = reader.readLine()) == null) {
                    consolePrinter.printLineToConsole("could not read from server");
                    break;
                }
            } catch (IOException exception) {
                consolePrinter.printLineToConsole("could not read from server");
                break;
            }
            if (inputLine.startsWith("send-file")) {
                Thread fileReceiveHandler = new FileReceiveHandler(inputLine, in, consolePrinter);
                fileReceiveHandler.start();
            } else {
                consolePrinter.printLineToConsole(inputLine);
            }
        }
    }
}

