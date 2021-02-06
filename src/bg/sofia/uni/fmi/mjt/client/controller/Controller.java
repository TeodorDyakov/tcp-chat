package bg.sofia.uni.fmi.mjt.client.controller;

import bg.sofia.uni.fmi.mjt.client.presenter.Presenter;
import bg.sofia.uni.fmi.mjt.client.service.FileTransferService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Controller {

    static final String SEND_FILE_COMMAND = "send-file-to";
    final String HOST;
    final int PORT;
    Presenter presenter;
    PrintWriter messagesWriter;
    BufferedReader messageReader;

    OutputStream fileTransferOutputStream;
    InputStream fileTransferInputStream;

    FileTransferService fileTransferService;

    BlockingQueue<String> toShow = new LinkedBlockingDeque<>();

    public Controller(Presenter presenter, String host, int port) {
        this.presenter = presenter;
        HOST = host;
        PORT = port;
    }

    public void handleIncomingData() {
        String inputLine;
        while (true) {
            try {
                if ((inputLine = messageReader.readLine()) == null) {
                    toShow.add("[ Disconnected from server! ]");
                    break;
                } else {
                    if (inputLine.startsWith(SEND_FILE_COMMAND)) {
                            handleFileReceive(inputLine);
                    } else {
                        toShow.add(inputLine);
                    }
                }
            } catch (IOException exception) {
                toShow.add("[ Disconnected from server! ]");
                break;
            }
        }
    }

    public void handleFileSendRequest(String input) {
        String[] tokens = input.split("\\s+");
        if (tokens.length < 3) {
            toShow.add("[ Not enough arguments! ]");
        }
        String filePath = tokens[2];
        String username = tokens[1];
        long fileSz = FileTransferService.getFileSize(filePath);
        String request =
            "%s %s %s %d".formatted(tokens[0], username, FileTransferService.getFileName(filePath), fileSz);
        messagesWriter.println(request);
        new Thread(() -> {
            boolean fileSent = fileTransferService.sendFile(filePath);
            if (!fileSent) {
                toShow.add("[ error when sending file ]");
            }
        }).start();
    }

    public void handleFileReceive(String input) {
        String[] tokens = input.split("\\s+");
        if (tokens.length != 4) {
            return;
        }
        String fileName = tokens[2];
        long fileSz = Long.parseLong(tokens[3]);
        new Thread(() -> {
            boolean fileReceived = fileTransferService.receiveFile(fileName, fileSz);
            if (!fileReceived) {
                toShow.add("[ file receive unsuccessful ]");
            } else {
                toShow.add("[ file " + fileName + " received ]");
            }
        }).start();
    }

    public void handleOutcomingData() {
        while (true) {
            String userInput = presenter.getInput();
            if (userInput.startsWith(SEND_FILE_COMMAND)) {
                handleFileSendRequest(userInput);
            } else {
                messagesWriter.println(userInput);
            }
        }
    }

    public void start() {
        Socket messagesSocket = null;
        Socket fileTransferSocket = null;

        try {
            messagesSocket = new Socket(HOST, PORT);
            fileTransferSocket = new Socket(HOST, PORT);
            this.fileTransferOutputStream = fileTransferSocket.getOutputStream();
            this.fileTransferInputStream = fileTransferSocket.getInputStream();
            messageReader = new BufferedReader(new InputStreamReader(messagesSocket.getInputStream()));
            messagesWriter = new PrintWriter(messagesSocket.getOutputStream(), true);
            fileTransferService = new FileTransferService(fileTransferOutputStream, fileTransferInputStream);
            new Thread(this::handleIncomingData).start();
            new Thread(this::handleOutcomingData).start();
        } catch (IOException e) {
            try {
                messagesSocket.close();
            } catch (IOException exception) {
                System.err.println(Arrays.toString(exception.getStackTrace()));
            }
            try {
                fileTransferSocket.close();
            } catch (IOException exception) {
                System.err.println(exception.getStackTrace());
            }
            System.err.println(e.getStackTrace());
            toShow.add("[ cant connect to server ]");
        }
        startPresenterCommunication();
    }

    public void startPresenterCommunication() {
        while (true) {
            try {
                presenter.updateView(toShow.take());
            } catch (InterruptedException e) {
                toShow.add("[ Fatal error! Close the app]");
                break;
            }
        }
    }

}
