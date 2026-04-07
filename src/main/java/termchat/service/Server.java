package termchat.service;
import termchat.model.ChatRoom;
import termchat.model.Message;
import termchat.model.User;
import termchat.repository.MessageRepository;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final List<User> users;
    private final List<ChatRoom> chatRooms;
    private final Map<String, Session> activeSessions;
    private final List<ClientHandler> clientHandlers;
    private final MessageRepository messageRepository;

    public Server() {
        this.users = null;
        this.chatRooms = null;
        this.activeSessions = new ConcurrentHashMap<>();
        this.clientHandlers = new ArrayList<>();
        this.messageRepository = new MessageRepository();
    }

    // Message isendi loomise meetod
    private Message createAndStoreMessage (String content, ClientHandler sender) {
        Message message = new Message(messageRepository.getAllMessages().size() +1, content, sender.getUser(), LocalDateTime.now());
        messageRepository.saveMessage(message);
        return message;
    }

    protected void routeMessage(String content, ClientHandler sender) {
        Message storedMessage = createAndStoreMessage(content, sender);
        storedMessage.markAsDelivered();

        String outboundMessage = storedMessage.getSender().getUsername() + ": " + storedMessage.getContent();

        // lisasin selle selleks, et saaks ise ka aru, kas sõnum läks teele
        sender.sendToClient(outboundMessage);

        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler != sender) {
                clientHandler.sendToClient(storedMessage.getContent());
            }
        }
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

    protected synchronized void addClientHandler (ClientHandler handler) {
        clientHandlers.add(handler);
    }

    protected synchronized void removeClientHandler (ClientHandler handler) {
        clientHandlers.remove(handler);
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
