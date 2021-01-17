package bg.sofia.uni.fmi.mjt.client;

import java.io.PrintWriter;

public class MessageSender {
    public static synchronized void sendMessage(PrintWriter writer, String msg) {
        writer.println(msg);
    }
}
