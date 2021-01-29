package bg.sofia.uni.fmi.mjt.client;

public class ConsolePrinter {

    public void printLineToConsole(String line) {
        synchronized (System.out){
            System.out.println(line);
        }
    }
}
