package termchat.client;

import termchat.model.User;
import termchat.command.CommandContext;
import termchat.command.CommandRegistry;
import termchat.server.Server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Server server;
    private final Socket socket;
    private User user;

    private DataInputStream in;
    private DataOutputStream out;

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
        if (user != null) {
            user.setClientHandler(this);
        }
    }

    private void setupStreams() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("ClientHandler stream setup error: " + e.getMessage());
        }
    }

    public void sendToClient(String message) {
        try {
            if (out != null) {
                out.writeInt(1); // message type
                out.writeUTF(message);
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("Error" + e.getMessage());
        }
    }

    private void listenLoop() {
        String messageIn;
        try {
            while (running) {
                int type = in.readInt(); // 1 - message, 2 - file
                if (type == 1) {
                    messageIn = in.readUTF();
                    handleMessage(messageIn);
                } else if (type == 2) {
                    server.FileHandler().receiveFile(user.getActiveChat(), user);
                }
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

    public DataInputStream getIn() {
        return in;
    }

    public DataOutputStream getOut() {
        return out;
    }
}
