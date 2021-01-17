package bg.sofia.uni.fmi.mjt.client;

public class ConsolePrinter {

    public synchronized void printLineToConsole(String line) {
        System.out.println(line);
    }
}
