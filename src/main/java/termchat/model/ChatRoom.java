package termchat.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatRoom {

    private final String ID;
    private String name;
    private final List<User> members = new ArrayList<>();
    private final List<Message> messages = new ArrayList<>();

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getMembers() {
        return members;
    }

    public ChatRoom(String name, User user) {
        this.name = name;
        this.ID = UUID.randomUUID().toString();
        this.members.add(user);
    }

    public String getID() {
        return ID;
    }

    public void addUser(User user) {
        members.add(user);
    }

    public void removeUser(User user) {
        members.remove(user);
    }

    public void broadcastMessage(Message message) {
        messages.add(message);
    }

    public User getUserByName(String username) {
        return members.stream()
                .filter(p -> p.getUsername().equalsIgnoreCase(username))
                .findFirst().orElse(null);
    }

    public List<Message> getHistory() {
        return messages;
    }
}
