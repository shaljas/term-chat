package termchat.repository;

import termchat.model.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageRepository {
    private final List<Message> messages;

    public MessageRepository() {
        this.messages = new ArrayList<>();
    }

    // Server saab sõnumi kätte ja salvestab selle meetodi abil Message isendi
    public void saveMessage(Message message) {
        messages.add(message);
    }

    // Selle meetodiga saaks tulevikus kuvada ajalugu ala, et teeme chat history
    public List<Message> getAllMessages() {
        return new ArrayList<>(messages);
    }
}
