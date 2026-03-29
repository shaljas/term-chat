package termchat.service;
import termchat.model.ChatRoom;
import termchat.model.User;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final List<User> users;
    private final List<ChatRoom> chatRooms;
    private final Map<String, Session> activeSessions;

    public Server() {
        this.users = null;
        this.chatRooms = null;
        this.activeSessions = new ConcurrentHashMap<>();
    }

    protected void start() throws IOException {
        ExecutorService pool = Executors.newFixedThreadPool(6);

        try (ServerSocket serverSocket = new ServerSocket(3000)){
            System.out.println("Server is now listening.");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                pool.execute(new ClientHandler(clientSocket, this));
            }
        } finally {
            pool.shutdown();
            activeSessions.forEach((k,v) -> v.endSession());
        }
    }

    protected void addSession(Session session) {
        activeSessions.put(session.getSessionId(), session);
    }

    protected void removeSession(Session session) {
        activeSessions.remove(session.getSessionId());
    }


    protected void createRoom(){}

    protected void registerUser(){}

    private void authenticateUser(){}

    private void routeMessage(){}

    private void notifyOfflineUser(){}
}
