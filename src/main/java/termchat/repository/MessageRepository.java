package termchat.repository;

import termchat.model.ChatRoom;
import termchat.model.Message;
import termchat.persistence.JsonStorageService;
import termchat.persistence.StoredMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageRepository {
    private static final String messages_file = "messages.json";

    private final List<Message> messages;
    private final List<StoredMessage> storedMessages;
    private final JsonStorageService storageService = new JsonStorageService();

    public MessageRepository() {
        this.messages = new ArrayList<>();
        this.storedMessages = new ArrayList<>();
    }

    // Server saab sõnumi kätte ja salvestab selle meetodi abil Message isendi
    public void saveMessage(Message message, ChatRoom chatRoom) {
        messages.add(message);

        StoredMessage storedMessage = new StoredMessage(
                message.getMessageId(),
                chatRoom.getName(),
                message.getSender().getUsername(),
                message.getContent(),
                message.getTimestamp().toString(),
                message.isDelivered()
        );

        storedMessages.add(storedMessage);
        saveMessagesToStorage();
    }

    private void saveMessagesToStorage() {
        storageService.save(messages_file, storedMessages);
    }

    // Selle meetodiga saaks tulevikus kuvada ajalugu ala, et teeme chat history
    public List<Message> getAllMessages() {
        return new ArrayList<>(messages);
    }
}
