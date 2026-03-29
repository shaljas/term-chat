package termchat.service;

import java.io.IOException;

public class ServerApp {
    public static void main() throws IOException {
        Server server = new Server();
        server.start();
    }
}
