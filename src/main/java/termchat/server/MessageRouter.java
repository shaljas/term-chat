package termchat.server;

import termchat.client.ClientHandler;
import termchat.client.OutputChannel;
import termchat.model.ChatRoom;
import termchat.model.Message;
import termchat.model.User;
import termchat.persistence.StoredMessage;
import termchat.repository.MessageRepository;
import termchat.repository.UserRepository;
import termchat.service.EmailService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static termchat.model.Ansi.*;

public class MessageRouter {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    public MessageRouter(MessageRepository messageRepository, UserRepository userRepository, SessionManager sessionManager) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
    }

    public void routeMessage(String content, OutputChannel sender) {
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

        Message storedMessage = createAndStoreMessage(content, sender);
        storedMessage.markAsDelivered();

        messageRepository.saveMessage(storedMessage, sendInRoom);
        sendInRoom.broadcastMessage(storedMessage);

        synchronized (this) {
            for (ClientHandler clientHandler : sessionManager.getActiveClients()) {
                User receiver = clientHandler.getUser();

                if (receiver != null && receiver.getActiveChat() == sendInRoom) {
                    clientHandler.sendToClient(storedMessage.format());
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

    public void broadcastSystemMessage(ChatRoom room, String message) {
        synchronized (this) {
            for (ClientHandler clientHandler : sessionManager.getActiveClients()) {
                User receiver = clientHandler.getUser();

                if (receiver != null && receiver.getActiveChat() == room) {
                    clientHandler.sendToClient(YELLOW + "[system] " + message + RESET);
                }
            }
        }
    }

    public void deliverPendingDMs(User user, OutputChannel outputChannel) {
        List<StoredMessage> pending = messageRepository.getUndeliveredDMs(user.getUsername());
        if (pending.isEmpty()) return;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        for (StoredMessage dm : pending) {
            LocalDateTime ts = LocalDateTime.parse(dm.getTimestamp());
            outputChannel.sendToClient(MAGENTA + "[" + ts.format(formatter) + "] [private from " + dm.getSenderUsername() + "] " + dm.getContent() + RESET);
        }
        messageRepository.markDMsAsDelivered(user.getUsername());
    }

    private Message createAndStoreMessage (String content, OutputChannel sender) {
        return new Message(messageRepository.getAllMessages().size() +1, content, sender.getUser(), LocalDateTime.now());
    }
}
