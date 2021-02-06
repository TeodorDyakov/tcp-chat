package bg.sofia.uni.fmi.mjt.client.service;

import bg.sofia.uni.fmi.mjt.server.exceptions.ExceptionLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileTransferService {

    public final static String receivedFilesDir = "received_files";
    private final OutputStream out;
    private final InputStream in;
    ExceptionLogger logger = new ExceptionLogger("client_exceptions.txt");

    public FileTransferService(OutputStream out, InputStream in) {
        this.out = out;
        this.in = in;
        new File(receivedFilesDir).mkdir();
    }

    public static long getFileSize(String filePath) {
        return new File(filePath).length();
    }

    public static String getFileName(String filePath) {
        return new File(filePath).getName();
    }

    public boolean sendFile(String filePath) {
        final var file = new File(filePath);
        final var bytes = new byte[8192];
        InputStream in = null;
        synchronized (out) {
            try {
                in = new FileInputStream(file);
                int count;
                while ((count = in.read(bytes)) > 0) {
                    out.write(bytes, 0, count);
                }
                in.close();
                out.flush();
            } catch (IOException e) {
                try {
                    in.close();
                } catch (IOException exception) {
                    logger.writeExceptionToFile(exception);
                }
                logger.writeExceptionToFile(e);
                return false;
            }
        }
        return true;
    }

    public boolean receiveFile(String fileName, long fileSz) {
        final var bytes = new byte[8192];
        long bytesLeftToRead = fileSz;
        long count;

        final var receivedFile = new File(receivedFilesDir + "/" + fileName);
        OutputStream out = null;
        try {
            receivedFile.createNewFile();
            out = new FileOutputStream(receivedFile);
            while ((count = in.read(bytes, 0, (int) Math.min(bytes.length, bytesLeftToRead))) > 0) {
                out.write(bytes, 0, (int) count);
                bytesLeftToRead -= count;
            }
            return true;
        } catch (IOException e) {
            logger.writeExceptionToFile(e);
            receivedFile.delete();
            try {
                out.close();
            } catch (IOException exception) {
                logger.writeExceptionToFile(exception);
            }
            return false;
        }
    }

}
