package bg.sofia.uni.fmi.mjt.server;

import org.junit.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;

public class ClientRequestHandlerTest {

    @Test
    public void handleRequestRegisterTest() {
        Database database = new Database(new StringReader(""), new StringWriter());
        database.readDatabaseToMemory();

        ClientRequestHandler clientRequestHandler = new ClientRequestHandler(null, new ConcurrentHashMap<>(),
            database);
        StringWriter writer = new StringWriter();
        clientRequestHandler.handleRequest("register kolio kote", writer);
        assertEquals(writer.toString(), ServerResponse.REGISTERED + System.lineSeparator() +
            "kolio has joined the chat" + System.lineSeparator());
    }

    @Test
    public void handleRequestLoginTest() {
        Database database = new Database(new StringReader("tedy:123"), null);
        database.readDatabaseToMemory();
        ClientRequestHandler clientRequestHandler = new ClientRequestHandler(null, new ConcurrentHashMap<>(),
            database);
        StringWriter writer = new StringWriter();
        clientRequestHandler.handleRequest("login tedy 123", writer);
        assertEquals(writer.toString(), ServerResponse.LOGGED_IN + System.lineSeparator() +
            "tedy has joined the chat" + System.lineSeparator());
    }

}