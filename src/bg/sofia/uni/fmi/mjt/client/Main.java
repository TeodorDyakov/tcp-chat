package bg.sofia.uni.fmi.mjt.client;

import bg.sofia.uni.fmi.mjt.client.controller.Controller;
import bg.sofia.uni.fmi.mjt.client.presenter.ConsolePresenter;
import bg.sofia.uni.fmi.mjt.client.presenter.Presenter;

public class Main {

    public static void main(String[] args) {
        Presenter consolePresenter = new ConsolePresenter();
        Controller controller = new Controller(consolePresenter, "localhost", 54545);
        controller.start();
    }
}
