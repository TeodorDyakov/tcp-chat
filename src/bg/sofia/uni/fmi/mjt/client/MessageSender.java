package bg.sofia.uni.fmi.mjt.client;

import java.io.PrintWriter;

public class MessageSender {
    private final PrintWriter writer;

    MessageSender(PrintWriter writer) {
        this.writer = writer;
    }

    public void sendMessage(String msg) {
        synchronized (writer) {
            writer.println(msg);
        }
    }
}
