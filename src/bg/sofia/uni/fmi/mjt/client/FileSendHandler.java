package bg.sofia.uni.fmi.mjt.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileSendHandler extends Thread {
    private final String inputLine;
    private final MessageSender messageSender;
    private final OutputStream out;
    private final ConsolePrinter consolePrinter;

    public FileSendHandler(String inputLine, MessageSender messageSender, OutputStream out,
                           ConsolePrinter consolePrinter) {
        this.inputLine = inputLine;
        this.messageSender = messageSender;
        this.out = out;
        this.consolePrinter = consolePrinter;
    }

    public void sendFile() {
        final var tokens = inputLine.split("\\s+");
        if (tokens.length < 3) {
            consolePrinter.printLineToConsole("not enough arguments!");
            return;
        }
        final var file = new File(tokens[2]);
        final var fileSz = file.length();
        final var message = tokens[0] + " " + tokens[1] + " " + file.getName() + " " + fileSz;
        messageSender.sendMessage(message);

        final var bytes = new byte[8192];

        synchronized (out) {
            try {
                InputStream in = new FileInputStream(file);
                int count;
                while ((count = in.read(bytes)) > 0) {
                    out.write(bytes, 0, count);
                }
                in.close();
                out.flush();
            } catch (
                IOException e) {
                consolePrinter.printLineToConsole("error when sending file");
            }
        }
    }

    @Override
    public void run() {
        sendFile();
    }
}
