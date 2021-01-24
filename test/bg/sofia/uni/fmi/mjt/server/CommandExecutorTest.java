package bg.sofia.uni.fmi.mjt.server;

import org.junit.Test;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertTrue;

public class CommandExecutorTest {

    @Test
    public void loginTest() {
        Database database = new Database(new StringReader("ana:banana"), new StringWriter());
        database.readDatabaseToMemory();
        Map<String, PrintWriter> clientWriters = new ConcurrentHashMap<>();
        Map<String, OutputStream> outputStreamMap = new ConcurrentHashMap<>();
        PrintWriter guestWriter = new PrintWriter(new StringWriter());

        ClientRequestHandler clientRequestHandler = new ClientRequestHandler(null, null, clientWriters,
            database, outputStreamMap);

        clientWriters.put(clientRequestHandler.getCurrentGuestID(), guestWriter);
        outputStreamMap.put(clientRequestHandler.getCurrentGuestID(), OutputStream.nullOutputStream());
        CommandExecutor cmdExec = new CommandExecutor(clientRequestHandler);
        var res = cmdExec.execute(CommandCreator.newCommand("login tedy 123"));
        assertTrue(res.get(clientRequestHandler.getCurrentGuestID()).contains(ServerResponse.INVALID_USERNAME_OR_PASS));
        res = cmdExec.execute(CommandCreator.newCommand("login ana banana"));
        System.out.println(res);
        assertTrue(res.get("ana").contains(ServerResponse.LOGGED_IN));
    }
}