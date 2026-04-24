package termchat.service;

import termchat.model.User;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Server server;
    private final Socket socket;
    private User user;

    private BufferedReader in;
    private PrintWriter out;

    private boolean running = true;
    private final CommandRegistry commandRegistry = new CommandRegistry();

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    private void setupStreams() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("ClientHandler stream setup error: " + e.getMessage());
        }
    }

    public void sendToClient(String message) {
        if (out != null) out.println(message);
    }

    private void listenLoop() {
        String messageIn;
        try {
            while (running && (messageIn = in.readLine()) != null) {
                handleMessage(messageIn);
            }
        } catch (IOException e) {
            System.out.println("ClientHandler loop incoming message readline error: " + e.getMessage());
        }
    }

    private void handleMessage(String message) {
        CommandContext ctx = new CommandContext(this, server);
        commandRegistry.execute(message, ctx);
    }

    public void stop() {
        running = false;

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error while stopping client: " + e.getMessage());
        }
    }

    private void cleanup() {
        try {

            if (in != null) {
                in.close();
            }

            if (out != null) {
                out.close();
            }

        } catch (IOException e) {
            System.out.println("ClientHandler cleanup error: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " started.");
        try {
            setupStreams();
            server.addClientHandler(this);
            listenLoop();

        } finally {
            server.removeClientHandler(this);
            cleanup();
            System.out.println(Thread.currentThread().getName() + " finished.");
        }
    }
}
