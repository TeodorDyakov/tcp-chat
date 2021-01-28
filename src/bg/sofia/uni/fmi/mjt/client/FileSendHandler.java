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
        String[] tokens = inputLine.split("\\s+");
        if (tokens.length < 3) {
            consolePrinter.printLineToConsole("not enough arguments!");
            return;
        }
        File file = new File(tokens[2]);
        long fileSz = file.length();
        String message = tokens[0] + " " + tokens[1] + " " + file.getName() + " " + fileSz;
        messageSender.sendMessage(message);

        byte[] bytes = new byte[16 * 1024];

        synchronized (out) {
            try {
                InputStream in = new FileInputStream(file);
                int count;
                while ((count = in.read(bytes)) > 0) {
                    out.write(bytes, 0, count);
                }
                in.close();
                out.flush();
                messageSender.sendMessage("File sent");
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
