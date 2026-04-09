package termchat.model;

import java.util.List;

public class User {
    private String userId;
    private String username;
    private String passwordHash;
    private boolean isOnline;

    public User(String userId, String username, String passwordHash) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.isOnline = false;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() { return passwordHash; }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
