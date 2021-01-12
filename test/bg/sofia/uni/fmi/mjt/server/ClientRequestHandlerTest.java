package bg.sofia.uni.fmi.mjt.server;

import org.junit.Test;

import javax.xml.crypto.Data;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

public class ClientRequestHandlerTest {

    @Test
    public void handleRequestRegisterTest() throws FileNotFoundException {
        Database database = new Database();
        database.readDatabaseToMemory(new StringReader(""));
        ClientRequestHandler clientRequestHandler = new ClientRequestHandler(null, new ConcurrentHashMap<>(),
            database);
        StringWriter writer = new StringWriter();
        clientRequestHandler.handleRequest("register kolio kote", writer);
        assertEquals(writer.toString(), ServerResponse.REGISTERED + System.lineSeparator() +
            "kolio has joined the chat" + System.lineSeparator());
    }

    @Test
    public void handleRequestLoginTest() throws FileNotFoundException {
        Database database = new Database();
        database.readDatabaseToMemory(new StringReader("tedy:123"));
        ClientRequestHandler clientRequestHandler = new ClientRequestHandler(null, new ConcurrentHashMap<>(),
            database);
        StringWriter writer = new StringWriter();
        clientRequestHandler.handleRequest("login tedy 123", writer);
        assertEquals(writer.toString(), ServerResponse.LOGGED_IN + System.lineSeparator() +
            "tedy has joined the chat" + System.lineSeparator());
    }

}