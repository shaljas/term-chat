package termchat.service;
import termchat.model.ChatRoom;
import termchat.model.Message;
import termchat.model.User;
import termchat.repository.MessageRepository;
import termchat.repository.UserRepository;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static termchat.service.EncryptionService.encryptPassword;

public class Server {
    private final List<ClientHandler> clientHandlers;
    private final List<ChatRoom> chatRooms;

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public Server() {
        this.chatRooms = new ArrayList<>();
        this.clientHandlers = new ArrayList<>();
        this.messageRepository = new MessageRepository();
        this.userRepository = new UserRepository();
    }

    private Message createAndStoreMessage (String content, ClientHandler sender) {
        Message message = new Message(messageRepository.getAllMessages().size() +1, content, sender.getUser(), LocalDateTime.now());
        messageRepository.saveMessage(message);
        return message;
    }

    protected void routeMessage(String content, ClientHandler sender) {
        Message storedMessage = createAndStoreMessage(content, sender);
        storedMessage.markAsDelivered();

        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler != sender) {
                clientHandler.sendToClient(
                        storedMessage.getSender().getUsername() + ": " + storedMessage.getContent());
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
        }
    }

    protected synchronized void addClientHandler (ClientHandler handler) {
        clientHandlers.add(handler);
    }

    protected synchronized void removeClientHandler (ClientHandler handler) {
        clientHandlers.remove(handler);
    }

    protected void createRoom(){}

    public List<User> getOnlineUsers() {
        return clientHandlers.stream().map(ClientHandler::getUser).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public synchronized String registerUser(String username, String password) {
        if (userRepository.usernameExists(username)) {
            return "Username already taken";
        }

        User newUser = new User(UUID.randomUUID().toString(), username, encryptPassword(password));
        userRepository.saveUser(newUser);
        return null; // null = success
    }

    public synchronized User loginUser(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(u -> u.getPasswordHash().equals(encryptPassword(password)))
                .orElse(null);
    }
}
