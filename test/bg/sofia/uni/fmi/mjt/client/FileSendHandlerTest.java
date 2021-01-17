package bg.sofia.uni.fmi.mjt.client;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class FileSendHandlerTest {
    @Test
    public void fileSendTest() throws IOException {
        final var file = new File("test.txt");
        file.createNewFile();
        final var fileContents = "hello from moscow";
        final var fileLength = 17;
        final var fileWriter = new FileWriter("test.txt", true);
        fileWriter.write(fileContents);
        final var stringWriter = new StringWriter();
        final var writer = new PrintWriter(stringWriter);
        final var messageSender = new MessageSender(writer);
        final var byteArrayOutputStream = new ByteArrayOutputStream();
        final var fileSendHandler = new FileSendHandler("send-file tedy A.txt", messageSender,
            byteArrayOutputStream,new ConsolePrinter());
        fileSendHandler.sendFile();
        assertEquals(stringWriter.toString(), "send-file tedy A.txt " + fileLength + System.lineSeparator());
        assertEquals(fileContents, byteArrayOutputStream.toString());
        file.delete();
    }
}