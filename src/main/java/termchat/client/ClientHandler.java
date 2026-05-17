package termchat.client;

import termchat.model.User;
import termchat.command.CommandContext;
import termchat.command.CommandRegistry;
import termchat.server.*;
import termchat.service.AuthService;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class ClientHandler implements Runnable, OutputChannel {
    private final Socket socket;
    private User user;

    private final SessionManager sessionManager;
    private final FileTransfer fileTransfer;
    private final AuthService authService;
    private final ChatRoomFactory chatRoomFactory;
    private final MessageRouter messageRouter;

    private DataInputStream in;
    private DataOutputStream out;

    private boolean running = true;
    private final CommandRegistry commandRegistry = new CommandRegistry();
    private final Set<String> notifiedOfflineRecipients = new HashSet<>();

    public ClientHandler(Socket socket, SessionManager sessionManager, FileTransfer fileTransfer, AuthService authService, ChatRoomFactory chatRoomFactory, MessageRouter messageRouter) {
        this.socket = socket;
        this.sessionManager = sessionManager;
        this.fileTransfer = fileTransfer;
        this.authService = authService;
        this.chatRoomFactory = chatRoomFactory;
        this.messageRouter = messageRouter;
    }

    @Override
    public User getUser() {
        return user;
    }

    public DataInputStream getIn() {
        return in;
    }

    public DataOutputStream getOut() {
        return out;
    }

    @Override
    public void setUser(User user) {
        if (this.user != null) {
            this.user.setClientHandler(null);
        }
        this.user = user;
        if (user != null) {
            user.setClientHandler(this);
        }
    }

    public boolean shouldNotifyOffline(String username) {
        return notifiedOfflineRecipients.add(username);
    }

    @Override
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

    @Override
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

    @Override
    public String readStringInput() throws IOException{
        in.readInt();
        return in.readUTF();
    }

    private void handleMessage(String message) {
        CommandContext ctx = new CommandContext(this, authService, chatRoomFactory, messageRouter, fileTransfer);
        commandRegistry.execute(message, ctx);
    }

    private void setupStreams() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("ClientHandler stream setup error: " + e.getMessage());
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
                    fileTransfer.receiveFile(user.getActiveChat(), user);
                }
            }
        } catch (IOException e) {
            System.out.println("ClientHandler loop incoming message readline error: " + e.getMessage());
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
            sessionManager.addClient(this);
            listenLoop();

        } finally {
            sessionManager.removeClient(this);
            cleanup();
            System.out.println(Thread.currentThread().getName() + " finished.");
        }
    }
}
