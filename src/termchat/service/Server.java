package termchat.service;
import termchat.model.ChatRoom;
import termchat.model.User;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private List<User> users;
    private List<ChatRoom> chatRooms;
    private List<Session> activeSessions;

    public Server() {
//        this.users = users;
//        this.chatRooms = chatRooms;
//        this.activeSessions = activeSessions;
    }

    protected void start() throws IOException {
        ExecutorService pool = Executors.newFixedThreadPool(6);

        try (ServerSocket serverSocket = new ServerSocket(3000)){
            while (true) {
                Socket clientSocket = serverSocket.accept();
                pool.execute(() -> handleClient(clientSocket));
            }
        } finally {
            pool.shutdown();
            activeSessions.forEach(Session::endSession);
        }
    };

    protected void createRoom(){};

    protected void registerUser(){};

    private void handleClient(Socket clientSocket){

    };

    private void authenticateUser(){};

    private void routeMessage(){};

    private void notifyOfflineUser(){};
}
