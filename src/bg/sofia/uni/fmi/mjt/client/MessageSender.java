package bg.sofia.uni.fmi.mjt.client;

import java.io.PrintWriter;

public class MessageSender {
    PrintWriter writer;

    MessageSender(PrintWriter writer) {
        this.writer = writer;
    }

    public synchronized void sendMessage(String msg) {
        writer.println(msg);
    }
}
