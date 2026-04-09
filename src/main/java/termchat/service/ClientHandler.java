package termchat.service;

import termchat.model.User;

import java.io.*;
import java.net.Socket;

import static termchat.service.EncryptionService.encryptPassword;

public class ClientHandler implements Runnable {
    private final Server server;
    private final Socket socket;
    private Session session;
    private User user;

    private BufferedReader in;
    private PrintWriter out;

    private boolean running = true;
    private boolean authenticated = false;


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
        }

        String[] parts = message.split(" ");
        String command = parts[0].toLowerCase();

        // mdea kuidas paremini teha neid commande praegu
        if (command.equals("register") && parts.length == 3) {
            String error = server.registerUser(parts[1], parts[2]);
            if (error==null) {
                sendToClient("Account registered");
            } else {
                sendToClient("Error: " + error);
            }
        } else if (command.equals("login") && parts.length == 3) {
            User found = server.loginUser(parts[1], parts[2]);
            if (found != null) {
                this.user = found;
                this.authenticated = true;
                sendToClient("Logging successful, " + user.getUsername());
            } else {
                sendToClient("ERROR Invalid username or password");
            }

        } else {
            // if authenticated then you reach this
            server.routeMessage(message, this);
            // out.println("Message: '" + message + "'.");
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
