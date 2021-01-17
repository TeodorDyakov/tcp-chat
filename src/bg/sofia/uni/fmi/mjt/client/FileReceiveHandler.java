package bg.sofia.uni.fmi.mjt.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileReceiveHandler extends Thread {
    String inputLine;
    InputStream in;

    FileReceiveHandler(String inputLine, InputStream in) {
        this.inputLine = inputLine;
        this.in = in;
    }

    public void receiveFile() {
        String[] tokens = inputLine.split("\\s+");

        String fileName = tokens[2];
        int fileSz = Integer.parseInt(tokens[3]);

        byte[] bytes = new byte[8192];
        int bytesRead = 0;
        int count;

        File receivedFile = new File("received_files/" + fileName);
        try {
            receivedFile.createNewFile();
            FileOutputStream out;
            out = new FileOutputStream(receivedFile);
            while (bytesRead < fileSz && (count = in.read(bytes)) > 0) {
                out.write(bytes, 0, count);
                bytesRead += count;
            }
        } catch (IOException e) {
            ConsoleOutput.printLineToConsole(e.getMessage());
            receivedFile.delete();
        }
        ConsoleOutput.printLineToConsole("[ file " + fileName + " received ]");
    }

    @Override
    public void run() {
        receiveFile();
    }
}
