package termchat.service;

import termchat.model.ChatRoom;
import termchat.model.Message;
import termchat.model.User;
import termchat.persistence.StoredMessage;
import termchat.repository.MessageRepository;
import termchat.server.ChatRoomFactory;

import java.time.LocalDateTime;

public class HistoryLoaderService {
    private final MessageRepository messageRepository;
    private final ChatRoomFactory chatRoomFactory;
    private final AuthService authService;

    public HistoryLoaderService(MessageRepository messageRepository, ChatRoomFactory chatRoomFactory, AuthService authService) {
        this.messageRepository = messageRepository;
        this.chatRoomFactory = chatRoomFactory;
        this.authService = authService;
    }

    public void loadChatHistoryFromStorage() {
        for (StoredMessage storedMessage : messageRepository.getStoredMessages()) {
            ChatRoom chatRoom = chatRoomFactory.getRoomByName(storedMessage.getRoomName());
            User sender = authService.getUserRepository().findByUsername(storedMessage.getSenderUsername()).orElse(null);
            if (chatRoom == null || sender == null) continue;

            Message message = new Message(storedMessage.getMessageId(),storedMessage.getContent(),sender, LocalDateTime.parse(storedMessage.getTimestamp()));
            if (storedMessage.isDelivered()) message.markAsDelivered();

            messageRepository.addLoadedMessage(message);
            chatRoom.broadcastMessage(message);
        }
    }
}
