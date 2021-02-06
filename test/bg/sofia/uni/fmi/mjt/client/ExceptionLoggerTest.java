package bg.sofia.uni.fmi.mjt.client;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class ExceptionLoggerTest {

    @Test
    public void writeExceptionToFile() throws IOException {
        ExceptionLogger logger = new ExceptionLogger("test.txt");
        NullPointerException e = new NullPointerException();
        ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException = new ArrayIndexOutOfBoundsException();
        logger.writeExceptionToFile(e);
        logger.writeExceptionToFile(arrayIndexOutOfBoundsException);
        String actual = Files.readString(Path.of("test.txt"));
        assertTrue(actual.contains("ArrayIndexOutOfBounds"));
        new File("test.txt").delete();
    }
}