package bg.sofia.uni.fmi.mjt.server;

import org.junit.Test;

import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DatabaseTest {

    StringReader databaseReader = new StringReader("tedy:123\nsandra:kote");

    @Test
    public void getPassOfUser() {
        Database database = new Database(databaseReader, null);
        database.readDatabaseToMemory();
        assertEquals(database.getPassOfUser("tedy"), "123");
    }

    @Test
    public void containsUser() {
        Database database = new Database(databaseReader, null);
        database.readDatabaseToMemory();
        assertTrue(database.containsUser("sandra"));
    }

    @Test
    public void savePassAndName() {

        StringWriter writer = new StringWriter();
        Database database = new Database(null, writer);

        database.savePassAndName("gosho", "mishka");
        assertTrue(database.containsUser("gosho"));
        assertEquals("mishka", database.getPassOfUser("gosho"));
        assertEquals("gosho:mishka\n", writer.toString());
    }
}