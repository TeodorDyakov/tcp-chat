package bg.sofia.uni.fmi.mjt.client.service;

import org.junit.Test;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;


public class FileTransferManagerTest {

    static final String testString = "some text".repeat(1000);

    @Test
    public void sendFile() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        var fileTransferService = new FileTransferService(baos, null);
        File file = new File("test.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter("test.txt"));
        writer.write(testString);
        writer.close();
        fileTransferService.sendFile("test.txt");
        assertEquals(testString, new String(baos.toByteArray()));
        baos.close();
        file.delete();
    }

    @Test
    public void receiveFile() throws IOException {
        InputStream targetStream = new ByteArrayInputStream(testString.getBytes());
        var fileTransferService = new FileTransferService(null, targetStream);
        fileTransferService.receiveFile("test.txt", testString.length());
        Path receivedFilePath = Path.of(FileTransferService.receivedFilesDir + "/" + "test.txt");
        String actual = Files.readString(receivedFilePath);
        assertEquals(actual, testString);
        Files.delete(receivedFilePath);
    }

    @Test
    public void getSize() {
        File file = new File("test.txt");
        assertEquals(FileTransferService.getFileSize("test.txt"), 0);
        file.delete();
    }

    @Test
    public void getName() {
        File file = new File("test.txt");
        assertEquals(FileTransferService.getFileName("test.txt"), "test.txt");
        file.delete();
    }
}