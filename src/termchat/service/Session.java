package termchat.service;

import java.time.LocalDateTime;

public class Session {
    private int sessionId;
    private LocalDateTime loginTime;
    private boolean active;

    protected void startSession(){};

    protected void endSession(){};
}
