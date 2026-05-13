package termchat.persistence;

public class StoredUser {
    private String userId;
    private String username;
    private String passwordHash;

    public StoredUser() {
    }

    public StoredUser(String userId, String username, String passwordHash) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
