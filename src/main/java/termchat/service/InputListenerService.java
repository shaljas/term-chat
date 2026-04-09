package termchat.service;

import java.io.BufferedReader;
import java.io.IOException;

public class InputListenerService {
    private final Thread inputThread;

    public InputListenerService(BufferedReader in) {
        this.inputThread = new Thread(() -> {
            try {
                String incoming;
                while((incoming = in.readLine()) != null) {
                    System.out.println(incoming);
                }
            } catch (IOException e) {
                System.out.println("Disconnected");
            }
        }, "input-thread");
    }

    public void start() {
        inputThread.start();
    }

    public void waitForCompleation() throws InterruptedException {
        inputThread.join();
    }
}
