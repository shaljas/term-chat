package termchat.service;

import termchat.model.User;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements Runnable {
    private final Server server;
    private final Socket socket;
    private Session session;
    private User user;

    private BufferedReader in;
    private PrintWriter out;

    private boolean running = true;
    private final Map<String, CommandHandler> commands = new HashMap<>();

    private void registerCommands() {
        commands.put("/register", args -> {
            if (user != null ) {
                sendToClient("Error: log out first");
                return;
            }
            if (args.length != 3) {
                sendToClient("Usage: register <username> <password>");
                return;
            };
            String error = server.registerUser(args[1], args[2]);

            if (error==null) {
                sendToClient("Account registered");
            } else {
                sendToClient("Error: " + error);
            }
        });

        commands.put("/login", args -> {
            if (user != null ) {
                sendToClient("Error: log out first");
                return;
            }
            if (args.length != 3) {
                sendToClient("Usage: login <username> <password>");
                return;
            };
            User found = server.loginUser(args[1], args[2]);

            if (found != null) {
                this.user = found;
                user.setOnline(true);
                sendToClient("Welcome " + this.user.getUsername());
            } else {
                sendToClient("Error: invalid username or password");
            }
        });

        commands.put("/logout", args -> {
            if (this.user == null) {
                sendToClient("You are not logged in");
                return;
            }
            sendToClient("Logging out, " + this.user.getUsername());
            user.setOnline(false);
            this.user = null;

        });

        commands.put("/help", args -> {
            sendToClient("/register <username> <password> - Creates a new account");
            sendToClient("/login <username> <password> - Log in to an account");
            sendToClient("/logout - logs the user out");
            sendToClient("/quit - stops the application");

        });

        commands.put("/quit", args -> running = false);
    }

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
        registerCommands();
        /*
        String testUsername = "user" + session.getSessionId().substring(0,5);
        this.user = new User(session.getSessionId(), testUsername, "");
        */
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
        String[] args = message.split(" ");
        String command = args[0].toLowerCase();

        commands.getOrDefault(command, a -> {
            if (this.user == null) {
                sendToClient("Unknown command. Type /help ");
            } else {
                server.routeMessage(message, this);
            }
        }).handle(args);

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
