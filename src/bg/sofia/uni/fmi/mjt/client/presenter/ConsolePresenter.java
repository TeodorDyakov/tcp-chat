package bg.sofia.uni.fmi.mjt.client.presenter;

import java.util.Scanner;

public class ConsolePresenter implements Presenter {

    Scanner scanner = new Scanner(System.in);

    @Override
    public void updateView(String msg) {
        System.out.println(msg);
    }

    @Override
    public String getInput() {
        return scanner.nextLine();
    }
}
