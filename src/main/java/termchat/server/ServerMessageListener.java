package termchat.server;

import termchat.client.ClientFileService;

import java.io.DataInputStream;
import java.io.IOException;

public class ServerMessageListener extends Thread {
    private final DataInputStream in;
    private boolean running = true;

    public ServerMessageListener(DataInputStream in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            String message;
            while (running) {
                int type = in.readInt();
                if (type == 1) {
                    message = in.readUTF();
                    System.out.println(message);
                } else if (type == 2) {
                    String result = ClientFileService.handleDownload(in);
                    System.out.println(result);
                }
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
