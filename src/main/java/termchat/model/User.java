package termchat.model;

import termchat.client.ClientHandler;

import java.util.List;

public class User {
    private String userId;
    private String username;
    private String passwordHash;
    private ChatRoom activeChat = null;
    private boolean isOnline;
    private ClientHandler clientHandler;

    public User(String userId, String username, String passwordHash) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
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
        List<Message> messagesToLoad = activeChat.getHistory();
        for (Message message : messagesToLoad) {
            getClientHandler().sendToClient(message.format());
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() { return passwordHash; }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
