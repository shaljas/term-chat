package termchat.persistence;

public class StoredUser {
    private String userId;
    private String username;
    private String passwordHash;
    private String email;

    public StoredUser() {
    }

    public StoredUser(String userId, String username, String passwordHash, String email) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
            this.email = email;
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

    public String getEmail() { return email; }
}
