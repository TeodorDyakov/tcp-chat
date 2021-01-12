package bg.sofia.uni.fmi.mjt.server;

import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DatabaseTest {

    StringReader databaseReader = new StringReader("tedy:123\nsandra:kote");

    @Test
    public void getPassOfUser() {
        Database database  = new Database();
        database.readDatabaseToMemory(databaseReader);
        assertEquals(database.getPassOfUser("tedy"), "123");
    }

    @Test
    public void containsUser() {
        Database database  = new Database();
        database.readDatabaseToMemory(databaseReader);
        assertTrue(database.containsUser("sandra"));
    }

    @Test
    public void savePassAndName() {
        Database database = new Database();
        database.savePassAndName("gosho", "mishka");
        assertTrue(database.containsUser("gosho"));
        assertEquals("mishka", database.getPassOfUser("gosho"));
    }
}