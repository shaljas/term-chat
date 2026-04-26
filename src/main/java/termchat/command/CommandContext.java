package termchat.command;

import termchat.client.ClientHandler;
import termchat.model.User;
import termchat.server.Server;

public record CommandContext(ClientHandler clientHandler, Server server) {

    public User getUser() {
        return clientHandler.getUser();
    }

    public void setUser(User user) {
        clientHandler.setUser(user);
    }

    public void send(String message) {
        clientHandler.sendToClient(message);
    }

    public void stop() {
        clientHandler.stop();
    }
}
