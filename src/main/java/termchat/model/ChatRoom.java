package termchat.model;

import java.util.ArrayList;
import java.util.List;
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

    public void rename(String newName) {
        this.name = newName;
    }

    public User getUserByName(String username) {
        for (User user : participants) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public void changeowner(String username, User user) {
        if (user != owner) return;
        this.owner = getUserByName(username);
    }

    public List<Message> getHistory() {
        return messages;
    }
}
