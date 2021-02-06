package bg.sofia.uni.fmi.mjt.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class ExceptionLogger {

    String filePath;

    ExceptionLogger(String filePath) {
        this.filePath = filePath;
    }

    public void writeExceptionToFile(Exception e) {
        try {
            File file = new File(filePath);
            FileWriter fw = new FileWriter(filePath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("Exception caught: " + String.valueOf(LocalDateTime.now()) + "\n");
            e.printStackTrace(new PrintWriter(bw));
            bw.flush();
            bw.close();
            System.out.println(file.getAbsolutePath());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
