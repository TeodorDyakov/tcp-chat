package bg.sofia.uni.fmi.mjt.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface BinaryIOdevice {
    InputStream getInputStream() throws IOException;
    OutputStream getOutputStream() throws IOException;
}
