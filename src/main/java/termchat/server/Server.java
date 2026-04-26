package termchat.server;
import termchat.client.ClientHandler;
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
    private final ChatRoomFactory crf;

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public Server() {
        this.chatRooms = new ArrayList<>();
        this.clientHandlers = new ArrayList<>();
        this.messageRepository = new MessageRepository();
        this.userRepository = new UserRepository();
        this.crf = new ChatRoomFactory(this.chatRooms, this.userRepository);
    }

    private Message createAndStoreMessage (String content, ClientHandler sender) {
        Message message = new Message(messageRepository.getAllMessages().size() +1, content, sender.getUser(), LocalDateTime.now());
        messageRepository.saveMessage(message);
        return message;
    }

    public void routeMessage(String content, ClientHandler sender) {
        User senderUser = sender.getUser();

        if (senderUser == null) {
            sender.sendToClient("Log in or register an account first.");
            return;
        }

        if (content == null || content.trim().isEmpty()) return;

        ChatRoom sendInRoom = sender.getUser().getActiveChat();

        if (sendInRoom == null) {
            sender.sendToClient("You are currently not in a chatroom.");
            return;
        }

        Message storedMessage = createAndStoreMessage(content,sender);
        storedMessage.markAsDelivered();

        sendInRoom.broadcastMessage(storedMessage);

        synchronized (this) {
            for (ClientHandler clientHandler : clientHandlers) {
                User receiver = clientHandler.getUser();

                if (receiver != null && receiver.getActiveChat() == sendInRoom) {
                    clientHandler.sendToClient(storedMessage.format());
                }
            }
        }
    }

    public void broadcastSystemMesaage(ChatRoom room, String message) {
        synchronized (this) {
            for (ClientHandler clientHandler : clientHandlers) {
                User receiver = clientHandler.getUser();

                if (receiver != null && receiver.getActiveChat() == room) {
                    clientHandler.sendToClient("[system] " + message);
                }
            }
        }
    }

    public String sendPrivateMessage(User sender, String receiverUsername, String content) {
        if (sender == null) {
            return "Log in or register an account first.";
        }

        if (receiverUsername == null || receiverUsername.trim().isEmpty()) {
            return "Usage: /msg <username> <message>";
        }

        if (content == null || content.trim().isEmpty()) {
            return "Private message cannot be empty.";
        }

        User receiver = userRepository.findByUsername(receiverUsername).orElse(null);

        if (receiver == null) {
            return "Could not find user " + receiverUsername + ".";
        }

        ClientHandler receiverHandler = receiver.getClientHandler();

        if (receiverHandler == null) {
            return "User " + receiver.getUsername() + " is not online.";
        }

        receiverHandler.sendToClient("[private from " + sender.getUsername() + "] " + content);
        return null;
    }

    public void start() throws IOException {
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

    public ChatRoomFactory RoomManager() {
        return crf;
    }

    public synchronized void addClientHandler (ClientHandler handler) {
        clientHandlers.add(handler);
    }

    public synchronized void removeClientHandler (ClientHandler handler) {
        clientHandlers.remove(handler);
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

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
