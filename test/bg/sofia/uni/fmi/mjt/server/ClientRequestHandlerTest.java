package bg.sofia.uni.fmi.mjt.server;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertTrue;

public class ClientRequestHandlerTest {

    @Test
    public void handleRequest() {
        Database database = new Database(new StringReader("ana:banana"), new StringWriter());
        database.readDatabaseToMemory();
        Map<String, PrintWriter> clientWriters = new ConcurrentHashMap<>();
        Map<String, OutputStream> outputStreamMap = new ConcurrentHashMap<>();
        StringWriter anaStringWriter = new StringWriter();

        PrintWriter anaWriter = new PrintWriter(anaStringWriter);
        PrintWriter guestWriter = new PrintWriter(new StringWriter());

        clientWriters.put("ana", anaWriter);

        ClientRequestHandler clientRequestHandler = new ClientRequestHandler(null, null, clientWriters,
            database, outputStreamMap);

        clientWriters.put(clientRequestHandler.getCurrentGuestID(), guestWriter);
        outputStreamMap.put(clientRequestHandler.getCurrentGuestID(), OutputStream.nullOutputStream());

        clientRequestHandler.handleRequest("register tedy 123");

        clientRequestHandler.handleRequest("send-msg \"hello from moscow\"");
        assertTrue(anaStringWriter.toString().contains("hello from moscow"));

        clientRequestHandler.handleRequest("send-msg-to ana \"hi\"");
        assertTrue(anaStringWriter.toString().contains("hi"));
    }

    @Test
    public void fileTransferTest() {
        Database database = new Database(new StringReader("ana:banana"), new StringWriter());
        database.readDatabaseToMemory();
        Map<String, PrintWriter> clientWriters = new ConcurrentHashMap<>();
        Map<String, OutputStream> outputStreamMap = new ConcurrentHashMap<>();
        StringWriter anaStringWriter = new StringWriter();

        PrintWriter anaWriter = new PrintWriter(anaStringWriter);

        ClientRequestHandler clientRequestHandler = new ClientRequestHandler(null, null, clientWriters,
            database, outputStreamMap);

        clientWriters.put(clientRequestHandler.getCurrentGuestID(), anaWriter);
        outputStreamMap.put(clientRequestHandler.getCurrentGuestID(), OutputStream.nullOutputStream());
        outputStreamMap.put("ana", OutputStream.nullOutputStream());
        clientRequestHandler.loginUser("ana");
        String initialString = "text";
        InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());

        clientRequestHandler.transferFile(targetStream, "send-file ana A.txt 4");
        assertTrue(anaStringWriter.toString().contains(ServerResponse.FILE_SENT_SUCCESSFULLY));
    }

}