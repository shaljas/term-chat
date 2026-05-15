package termchat.server;
import termchat.client.ClientHandler;
import termchat.exceptions.UsernameTakenException;
import termchat.model.ChatRoom;
import termchat.model.Message;
import termchat.model.User;
import termchat.persistence.StoredMessage;
import termchat.repository.MessageRepository;
import termchat.repository.UserRepository;
import termchat.service.EmailService;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static termchat.model.Ansi.*;
import static termchat.service.EncryptionService.encryptPassword;

public class Server {
    private final List<ClientHandler> clientHandlers;
    private final List<ChatRoom> chatrooms;
    private final ChatRoomFactory chatRoomFactory;
    private final FileTransfer fileTransfer;

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    private static final int PORT = 3000;
    private static final int THREAD_POOL_SIZE = 6;
    private static final String FILE_STORAGE_PATH = "Data/files";

    public Server() {
        this.chatrooms = new ArrayList<>();
        this.clientHandlers = new ArrayList<>();
        this.messageRepository = new MessageRepository();
        this.userRepository = new UserRepository();
        this.chatRoomFactory = new ChatRoomFactory(this.chatrooms, this.userRepository);
        this.fileTransfer = new FileTransfer(this, FILE_STORAGE_PATH);
        loadChatHistoryFromStorage();
    }

    private void loadChatHistoryFromStorage() {
        for (StoredMessage storedMessage : messageRepository.getStoredMessages()) {
            ChatRoom chatRoom = chatRoomFactory.getRoomByName(storedMessage.getRoomName());
            User sender = userRepository.findByUsername(storedMessage.getSenderUsername()).orElse(null);
            if (chatRoom == null || sender == null) continue;

            Message message = new Message(storedMessage.getMessageId(),storedMessage.getContent(),sender,LocalDateTime.parse(storedMessage.getTimestamp()));
            if (storedMessage.isDelivered()) message.markAsDelivered();

            messageRepository.addLoadedMessage(message);
            chatRoom.broadcastMessage(message);
        }
    }

    private Message createAndStoreMessage (String content, ClientHandler sender) {
        return new Message(messageRepository.getAllMessages().size() +1, content, sender.getUser(), LocalDateTime.now());
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

        messageRepository.saveMessage(storedMessage, sendInRoom);
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

    public void broadcastSystemMessage(ChatRoom room, String message) {
        synchronized (this) {
            for (ClientHandler clientHandler : clientHandlers) {
                User receiver = clientHandler.getUser();

                if (receiver != null && receiver.getActiveChat() == room) {
                    clientHandler.sendToClient(YELLOW + "[system] " + message + RESET);
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

        Message dm = new Message(
                messageRepository.getAllMessages().size() + 1,
                content,
                sender,
                LocalDateTime.now()
        );

        if (!receiver.isOnline()) {
            messageRepository.saveDM(dm, receiverUsername);
            ClientHandler senderHandler = sender.getClientHandler();
            if (senderHandler != null && senderHandler.shouldNotifyOffline(receiverUsername)) {
                String receiverEmail = receiver.getEmail();
                if (receiverEmail != null) {
                    EmailService.sendDMNotification(receiverEmail, sender.getUsername());
                }
            }
            return null;
        }

        receiverHandler.sendToClient(BOLD + "[private from " + sender.getUsername() + "] " + content + RESET);
        return null;
    }

    public void deliverPendingDMs(User user, ClientHandler handler) {
        List<StoredMessage> pending = messageRepository.getUndeliveredDMs(user.getUsername());
        if (pending.isEmpty()) return;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        for (StoredMessage dm : pending) {
            LocalDateTime ts = LocalDateTime.parse(dm.getTimestamp());
            handler.sendToClient(CYAN + "[" + ts.format(formatter) + "] [private from " + dm.getSenderUsername() + "] " + dm.getContent() + RESET);
        }
        messageRepository.markDMsAsDelivered(user.getUsername());
    }

    public void start() throws IOException {
        ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket serverSocket = new ServerSocket(PORT)){
            System.out.println("Server is now listening.");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                pool.execute(new ClientHandler(clientSocket, this));
            }
        } finally {
            pool.shutdown();
        }
    }

    public FileTransfer getFileHandler() {
        return fileTransfer;
    }

    public ChatRoomFactory getRoomManager() {
        return chatRoomFactory;
    }

    public synchronized void addClientHandler (ClientHandler handler) {
        clientHandlers.add(handler);
    }

    public synchronized void removeClientHandler (ClientHandler handler) {
        clientHandlers.remove(handler);
    }

    public synchronized void registerUser(String username, String password, String email) {
        if (userRepository.usernameExists(username)) {
            throw new UsernameTakenException(username);
        }

        User newUser = new User(UUID.randomUUID().toString(), username, encryptPassword(password), email);
        userRepository.saveUser(newUser);
    }

    public synchronized User loginUser(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(u -> u.getPasswordHash().equals(encryptPassword(password)))
                .orElse(null);
    }
}
