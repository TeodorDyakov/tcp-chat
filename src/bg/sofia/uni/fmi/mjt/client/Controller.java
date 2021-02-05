package bg.sofia.uni.fmi.mjt.client;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Controller {

    static final String SEND_FILE_COMMAND = "send-file-to";
    Presenter presenter;
    NetworkIO messagesIO;
    NetworkIO fileTransfer;
    FileTransferManager fileTransferManager;

    BlockingQueue<String> toShow = new LinkedBlockingDeque<>();

    Controller(Presenter presenter, NetworkIO messagesIO, NetworkIO fileTransfer){
        this.presenter = presenter;
        this.messagesIO = messagesIO;
        this.fileTransfer = fileTransfer;
        fileTransferManager = new FileTransferManager(fileTransfer.getOutputStream(), fileTransfer.getInputStream());
    }

    public void handleIncomingData(){
        String inputLine;
        while (true) {
            try {
                if ((inputLine = messagesIO.readLine()) == null) {
                    toShow.add("could not read from server");
                    break;
                }else{
                    if(inputLine.startsWith(SEND_FILE_COMMAND)) {
                        handleReceiveRequest(inputLine);
                    }else {
                        toShow.add(inputLine);
                    }
                }
            } catch (IOException exception) {
                toShow.add("could not read from server");
                break;
            }
        }
    }

    public void handleFileSendRequest(String input){
        String[] tokens = input.split("\\s+");
        if(tokens.length < 3){
            toShow.add("Not enough arguments!");
            return;
        }
        String filePath = tokens[2];
        String username = tokens[1];
        long fileSz =  FileTransferManager.getFileSize(filePath);
        String request =
            "%s %s %s %d".formatted(tokens[0], username, FileTransferManager.getFileName(filePath), fileSz);
        messagesIO.writeLine(request);
        new Thread(() -> {
            boolean fileSent = fileTransferManager.sendFile(filePath);
            if(!fileSent){
                toShow.add("error when sending file");
            }
        }).start();
    }

    public void handleReceiveRequest(String input){
        String[] tokens = input.split("\\s+");
        if(tokens.length != 4){
            return;
        }
        String fileName = tokens[2];
        long fileSz =  Long.parseLong(tokens[3]);
        new Thread(() -> {
            boolean fileReceived = fileTransferManager.receiveFile(fileName, fileSz);
            if(!fileReceived){
                toShow.add("[ file receive unsuccessful ]");
            }else{
                toShow.add("[ file " + fileName +" received ]");
            }
        }).start();
    }

    public void handleOutcomingData(){
        while(true){
            String userInput = presenter.getInput();
            if(userInput.startsWith(SEND_FILE_COMMAND)) {
                handleFileSendRequest(userInput);
            }else {
                messagesIO.writeLine(userInput);
            }
        }
    }

    public void start() {
        new Thread(this::handleIncomingData).start();
        new Thread(this::handleOutcomingData).start();
        presentData();
    }

    public void presentData(){
        while(true){
            try {
                presenter.updateView(toShow.take());
            } catch (InterruptedException e) {
                toShow.add("Interrupted");
                break;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        NetworkIO networkIO = new NetworkIO(54545, "localhost");
        NetworkIO fileTransfer = new NetworkIO(54545, "localhost");

        Presenter consolePresenter = new ConsolePresenter();
        Controller controller = new Controller(consolePresenter, networkIO, fileTransfer);
        controller.start();
    }

}
