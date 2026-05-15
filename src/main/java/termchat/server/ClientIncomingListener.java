package termchat.server;

import termchat.client.ClientFileService;

import java.io.DataInputStream;
import java.io.IOException;

public class ClientIncomingListener extends Thread {
    private final DataInputStream in;
    private boolean running = true;
    private static final int TYPE_MESSAGE = 1;
    private static final int TYPE_FILE = 2;

    public ClientIncomingListener(DataInputStream in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            String message;
            while (running) {
                int type = in.readInt();
                if (type == TYPE_MESSAGE) {
                    message = in.readUTF();
                    System.out.println(message);
                } else if (type == TYPE_FILE) {
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
