package termchat.client;

import termchat.model.User;

import java.io.IOException;

public interface OutputChannel {
    void sendToClient(String message);
    String readStringInput() throws IOException;
    User getUser();
    void setUser(User user);
    void stop();
}
