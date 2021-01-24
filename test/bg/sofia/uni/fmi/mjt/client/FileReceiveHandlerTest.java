package bg.sofia.uni.fmi.mjt.client;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class FileReceiveHandlerTest {

    @Test
    public void receiveFileTest() {
        final var outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));

        final var fileContents = "file contents";
        final var fileLength = fileContents.length();
        final var inputStream = new ByteArrayInputStream("file contents".getBytes(StandardCharsets.UTF_8));

        final var fileReceiveHandler = new FileReceiveHandler("send-file tedy test.txt " + fileLength,
            inputStream, new ConsolePrinter());
        fileReceiveHandler.receiveFile();
        assertEquals("[ file test.txt received ]", outputStreamCaptor.toString()
            .trim());
        System.setOut(System.out);
    }
}