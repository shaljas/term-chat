package termchat.model;

public class User {
    private String userId;
    private String username;
    private String passwordHash;
    private ChatRoom activeChat = null;
    private boolean isOnline;

    public User(String userId, String username, String passwordHash) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.isOnline = false;
    }

    public ChatRoom getActiveChat() {
        return activeChat;
    }

    public void setActiveChat(ChatRoom activeChat) {
        this.activeChat = activeChat;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() { return passwordHash; }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
