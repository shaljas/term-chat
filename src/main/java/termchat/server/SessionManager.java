package termchat.server;

import termchat.client.ClientHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SessionManager {
    private final List<ClientHandler> activeClients = Collections.synchronizedList(new ArrayList<>());

    public void addClient(ClientHandler handler) {
        activeClients.add(handler);
    }

    public void removeClient(ClientHandler handler) {
        activeClients.remove(handler);
    }

    public List<ClientHandler> getActiveClients() {
        return activeClients;
    }
}
