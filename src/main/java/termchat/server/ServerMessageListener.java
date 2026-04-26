package termchat.server;

import java.io.BufferedReader;
import java.io.IOException;

public class ServerMessageListener extends Thread {
    private final BufferedReader in;
    private boolean running = true;

    public ServerMessageListener(BufferedReader in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            String message;
            while (running && (message = in.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            System.out.println("Server Message Listener error: failure to read. " + e.getMessage());
        }
    }

    public void shutdown() {
        running = false;
        interrupt();
    }
}
