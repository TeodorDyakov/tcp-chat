package bg.sofia.uni.fmi.mjt.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class FileSendHandler extends Thread {
    String inputLine;
    PrintWriter writer;
    OutputStream out;

    public FileSendHandler(String inputLine, PrintWriter writer, OutputStream out) {
        this.inputLine = inputLine;
        this.writer = writer;
        this.out = out;
    }

    public void sendFile() {
        String[] tokens = inputLine.split("\\s+");

        File file = new File(tokens[2]);
        long fileSz = file.length();
        MessageSender.sendMessage(writer, inputLine + " " + fileSz);

        byte[] bytes = new byte[16 * 1024];
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
            System.out.println("error when sending file");
        }
    }

    @Override
    public void run() {
        sendFile();
    }
}
