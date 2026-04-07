package termchat.service;

import termchat.model.User;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Server server;
    private final Socket socket;
    private Session session;
    private User user;

    private BufferedReader in;
    private PrintWriter out;

    private boolean running = true;

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

    private void startSession() {
        session = new Session();
        session.startSession();
        server.addSession(session);

        String testUsername = "user" + session.getSessionId().substring(0,5);
        this.user = new User(session.getSessionId(), testUsername, "");
    }

    private void listenLoop() {
        String message;
        try {
            while (running && (message = in.readLine()) != null) {
                handleMessage(message);
            }
        } catch (IOException e) {
            System.out.println("ClientHandler loop readline error: " + e.getMessage());
        }
    }

    private void handleMessage(String message) {
        if (message.equalsIgnoreCase("quit")) {
            running = false;
        } else {
            out.println("Message: '" + message + "'.");
        }
    }

    private void cleanup() {
        try {
            if (session != null) {
                session.endSession();
                server.removeSession(session);
            }

            if (in != null) {
                in.close();
            }

            if (out != null) {
                out.close();
            }

            if (socket != null) {
                socket.close();
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
            startSession();
            server.addClientHandler(this);
            listenLoop();
        } finally {
            server.removeClientHandler(this);
            cleanup();
            System.out.println(Thread.currentThread().getName() + " finished.");
        }
    }
}
