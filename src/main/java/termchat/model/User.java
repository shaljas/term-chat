package termchat.model;

import termchat.client.ClientHandler;

public class User {
    private final String userId;
    private String username;
    private String passwordHash;
    private ChatRoom activeChat = null;
    private boolean isOnline;
    private ClientHandler clientHandler;
    private String email;

    public boolean isOnline() {
        return isOnline;
    }

    public User(String userId, String username, String passwordHash, String email) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.isOnline = false;
    }

    public ChatRoom getActiveChat() {
        return activeChat;
    }

    public ClientHandler getClientHandler() {
        return clientHandler;
    }

    public void setClientHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    public void setActiveChat(ChatRoom activeChat) {
        this.activeChat = activeChat;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() { return passwordHash; }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getEmail() { return email; }
}
