package termchat.repository;

import com.google.gson.reflect.TypeToken;
import termchat.model.ChatRoom;
import termchat.model.Message;
import termchat.persistence.JsonStorageService;
import termchat.persistence.StoredMessage;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository {
    private static final String MESSAGES_FILE = "messages.json";

    private final List<Message> messages;
    private final List<StoredMessage> storedMessages;
    private final JsonStorageService storageService = new JsonStorageService();

    public MessageRepository() {
        this.messages = new ArrayList<>();
        this.storedMessages = new ArrayList<>();
        loadMessagesFromStorage();
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
        storageService.save(MESSAGES_FILE, storedMessages);
    }

    public void loadMessagesFromStorage() {
        Type messageListType = new TypeToken<List<StoredMessage>>() {}.getType();
        List<StoredMessage> loadedMessages = storageService.load(MESSAGES_FILE, messageListType);
        if (loadedMessages == null) return;
        storedMessages.addAll(loadedMessages);
    }

    // Selle meetodiga saaks tulevikus kuvada ajalugu ala, et teeme chat history
    public List<Message> getAllMessages() {
        return new ArrayList<>(messages);
    }

    public List<StoredMessage> getStoredMessages() {
        return new ArrayList<>(storedMessages);
    }

    public void addLoadedMessage(Message message) {
        messages.add(message);
    }
}
