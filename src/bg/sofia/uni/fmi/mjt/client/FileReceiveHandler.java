package bg.sofia.uni.fmi.mjt.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileReceiveHandler extends Thread {
    private final String inputLine;
    private final InputStream in;
    private final ConsolePrinter consolePrinter;

    FileReceiveHandler(String inputLine, InputStream in, ConsolePrinter consolePrinter) {
        this.inputLine = inputLine;
        this.in = in;
        this.consolePrinter = consolePrinter;
    }

    void receiveFile() {
        final var tokens = inputLine.split("\\s+");

        final var fileName = tokens[2];
        final var fileSz = Integer.parseInt(tokens[3]);

        final var bytes = new byte[8192];
        int bytesLeftToRead = fileSz;
        int count;

        final var receivedFile = new File("received_files/" + fileName);
        try {
            receivedFile.createNewFile();
            final var out = new FileOutputStream(receivedFile);
            while ((count = in.read(bytes, 0, Math.min(bytes.length, bytesLeftToRead))) > 0) {
                out.write(bytes, 0, count);
                bytesLeftToRead -= count;
            }
            consolePrinter.printLineToConsole("[ file " + fileName + " received ]");
        } catch (IOException e) {
            consolePrinter.printLineToConsole("File receive unsuccessful.");
            consolePrinter.printLineToConsole(e.getMessage());
            receivedFile.delete();
        }
    }

    @Override
    public void run() {
        receiveFile();
    }
}
