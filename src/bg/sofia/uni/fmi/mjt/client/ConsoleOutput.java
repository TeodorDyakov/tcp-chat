package bg.sofia.uni.fmi.mjt.client;

public class ConsoleOutput {
    public static synchronized void printLineToConsole(String line) {
        System.out.println(line);
    }
}
