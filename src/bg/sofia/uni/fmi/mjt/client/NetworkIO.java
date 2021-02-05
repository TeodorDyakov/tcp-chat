package bg.sofia.uni.fmi.mjt.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class NetworkIO implements BinaryIOdevice, StringIODevice{

    Socket socket;
    BufferedReader reader;
    PrintWriter writer;
    OutputStream outputStream;
    InputStream inputStream;

    NetworkIO(int port, String host) throws IOException {
        socket = new Socket(host,port);
        this.outputStream = socket.getOutputStream();
        this.inputStream = socket.getInputStream();
        reader = new BufferedReader(new InputStreamReader(inputStream));
        writer = new PrintWriter(outputStream, true);
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream(){
        return outputStream;
    }

    @Override
    public String readLine() throws IOException {
        return reader.readLine();
    }

    @Override
    public void writeLine(String s) {
        writer.println(s);
    }
}
