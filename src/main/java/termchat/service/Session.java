package termchat.service;

import java.time.LocalDateTime;
import java.util.UUID;

public class Session {
    private final String sessionId;
    private LocalDateTime loginTime;
    private boolean active;

    public Session() {
        sessionId = UUID.randomUUID().toString();
        active = false;
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void startSession(){
        loginTime = LocalDateTime.now();
        active = true;
    }

    public void endSession(){
        active = false;
    }
}
