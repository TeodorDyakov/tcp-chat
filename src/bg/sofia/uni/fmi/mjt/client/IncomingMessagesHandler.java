package bg.sofia.uni.fmi.mjt.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IncomingMessagesHandler extends Thread {

    BufferedReader reader;
    InputStream in;

    IncomingMessagesHandler(BufferedReader reader, InputStream in) {
        this.reader = reader;
        this.in = in;
    }

    public void receiveFile(InputStream in, String inputLine) throws IOException {
        String[] tokens = inputLine.split("\\s+");

        String fileName = tokens[2];
        int fileSz = Integer.parseInt(tokens[3]);

        byte[] bytes = new byte[16 * 1024];
        int bytesRead = 0;
        int count;

        File receivedFile = new File("received_files/" + fileName);
        receivedFile.createNewFile();
        var out = new FileOutputStream(receivedFile);

        while (bytesRead < fileSz && (count = in.read(bytes)) > 0) {
            out.write(bytes, 0, count);
            bytesRead += count;
        }
        out.flush();
        out.close();
        String response = reader.readLine();
        System.out.println(response);
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
            if (inputLine.startsWith("send-file")) {
                try {
                    receiveFile(in, inputLine);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            } else {
                System.out.println(inputLine);
            }
        }
    }
}

