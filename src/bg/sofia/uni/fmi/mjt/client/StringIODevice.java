package bg.sofia.uni.fmi.mjt.client;

import java.io.IOException;

public interface StringIODevice {

    String readLine() throws IOException;

    void writeLine(String s);
}
