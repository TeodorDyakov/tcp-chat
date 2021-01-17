package bg.sofia.uni.fmi.mjt.server;

import org.junit.Test;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClientRequestHandlerTest {

    @Test
    public void handleRequestRegisterTest() {
        Database database = new Database(new StringReader(""), new StringWriter());
        database.readDatabaseToMemory();

        ClientRequestHandler clientRequestHandler = new ClientRequestHandler(null, null, new ConcurrentHashMap<>(),
            database, new ConcurrentHashMap<>());
        StringWriter writer = new StringWriter();
        clientRequestHandler.handleRequest("register kolio kote", writer, OutputStream.nullOutputStream());
        assertEquals(writer.toString(), ServerResponse.REGISTERED + System.lineSeparator() +
            "kolio has joined the chat" + System.lineSeparator());
    }

    @Test
    public void handleRequestLoginTest() {
        Database database = new Database(new StringReader("tedy:123"), null);
        database.readDatabaseToMemory();
        ClientRequestHandler clientRequestHandler = new ClientRequestHandler(null, null, new ConcurrentHashMap<>(),
            database, new ConcurrentHashMap<>());
        StringWriter writer = new StringWriter();
        clientRequestHandler.handleRequest("login tedy 123", writer, OutputStream.nullOutputStream());
        assertEquals(writer.toString(), ServerResponse.LOGGED_IN + System.lineSeparator() +
            "tedy has joined the chat" + System.lineSeparator());
    }

    @Test
    public void handleRequestMessageTest() {
        Database database = new Database(new StringReader("tedy:123\nana:banana"), null);
        database.readDatabaseToMemory();
        ClientRequestHandler clientRequestHandler = new ClientRequestHandler(null, null, new ConcurrentHashMap<>(),
            database, new ConcurrentHashMap<>());
        StringWriter writer = new StringWriter();
        clientRequestHandler.handleRequest("send-msg-to tedy hello", writer, OutputStream.nullOutputStream());
        assertEquals(writer.toString(), ServerResponse.NOT_LOGGED_IN + System.lineSeparator());
        clientRequestHandler.handleRequest("login ana banana", writer, OutputStream.nullOutputStream());
        writer = new StringWriter();
        clientRequestHandler.handleRequest("send-msg-to tedy hello", writer, OutputStream.nullOutputStream());
        assertEquals(writer.toString(), "[ no user with this name online ]\n");
    }

    @Test
    public void handleRequestMessageUserOnlineTest() {
        Database database = new Database(new StringReader("tedy:123\nana:banana"), null);
        database.readDatabaseToMemory();
        Map<String, PrintWriter> clientWriters = new ConcurrentHashMap<>();

        StringWriter anaStringWriter = new StringWriter();
        PrintWriter anaWriter = new PrintWriter(anaStringWriter);
        clientWriters.put("ana", anaWriter);

        ClientRequestHandler clientRequestHandler = new ClientRequestHandler(null, null, clientWriters,
            database, new ConcurrentHashMap<>());

        StringWriter writer = new StringWriter();
        clientRequestHandler.handleRequest("login tedy 123", writer, OutputStream.nullOutputStream());
        clientRequestHandler.handleRequest("send-msg-to ana hello", writer, OutputStream.nullOutputStream());
        assertTrue(writer.toString().contains("hello"));
        assertTrue(anaStringWriter.toString().contains("hello"));
        clientRequestHandler.handleRequest("send-msg \"cats are cool\"", writer, OutputStream.nullOutputStream());
        assertTrue(anaStringWriter.toString().contains("cats are cool"));
    }

}