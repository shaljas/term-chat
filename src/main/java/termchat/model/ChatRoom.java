package termchat.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatRoom {

    private final String ID;
    private String name;
    private final List<User> participants;
    private List<Message> messages;

    public ChatRoom(String name) {
        this.name = name;
        this.ID = UUID.randomUUID().toString();
        this.participants = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    public String getID() {
        return ID;
    }

    public void addUser(User user) {
        participants.add(user);
    }

    public void removeUser(User user) {
        participants.remove(user);
    }

    public void broadcastMessage(Message message) {
        messages.add(message);
    }

    public List<Message> getHistory() {
        return messages;
    }
}
