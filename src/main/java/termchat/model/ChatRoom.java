package termchat.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ChatRoom {

    private final String ID;
    private String name;
    private final List<User> participants = new ArrayList<>();
    private User owner;
    private final List<Message> messages = new ArrayList<>();

    public String getName() {
        return this.name;
    }

    public User getOwner() {
        return owner;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public ChatRoom(String name, User owner) {
        this.name = name;
        this.ID = UUID.randomUUID().toString();
        this.participants.add(owner);
        this.owner = owner;
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

    public void rename(String newName, User user) {
        if (user != owner) return;
        this.name = newName;
    }

    public User getUserByName(String username) {
        return participants.stream()
                .filter(p -> p.getUsername().equalsIgnoreCase(username))
                .findFirst().orElse(null);
    }

    public void changeowner(String username, User user) {
        if (user != owner) return;
        User newOwner = getUserByName(username);
        if (newOwner != null) {
            this.owner = newOwner;
        }
    }

    public List<Message> getHistory() {
        return messages;
    }
}
