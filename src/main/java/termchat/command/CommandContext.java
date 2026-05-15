package termchat.command;

import termchat.client.ClientHandler;
import termchat.model.ChatRoom;
import termchat.model.User;
import termchat.server.Server;

import static termchat.model.Ansi.RED;
import static termchat.model.Ansi.RESET;

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

    public void sendError(String message) {
        clientHandler.sendToClient(RED + message + RESET);
    }

    public boolean requireLoggedOut() {
        if (this.getUser() != null) {
            this.sendError("Error: log out first");
            return true;
        }
        return false;
    }

    public boolean requireLoggedIn() {
        if (this.getUser() == null) {
            this.sendError("Log in or register an account first.");
            return true;
        }
        return false;
    }

    public boolean cannotExecuteChatroomCommands() {
        if (this.requireLoggedIn()) return true;

        if (this.getUser().getActiveChat() == null) {
            this.sendError("You are currently not in a chatroom.");
            return true;
        }

        return false;
    }

    public boolean requireArgCount(String[] args, int expected, CommandContext ctx, String usage) {
        if (args.length != expected) {
            ctx.sendError(usage);
            return true;
        }
        return false;
    }

    public User loginAndGetAccount(String[] args) {
        String username = args[1];
        String password = args[2];

        return server().loginUser(username, password);
    }

    public boolean isAccountInvalid(User account, String errorMessage) {
        if (account == null) {
            sendError("Error: " + errorMessage);
            return true;
        }
        return false;
    }

    public void loginOrRegister(String message, User account) {

        if (account.isOnline()) {
            sendError("User has already been logged in.");
            return;
        }

        setUser(account);
        account.setOnline(true);

        ChatRoom mainChat = server().getRoomManager().getMainChat();
        mainChat.addUser(account);
        account.setActiveChat(mainChat);

        new termchat.command.HistoryCommands().history(new String[]{"/history","5"}, this);
        send(message + account.getUsername());
        server().deliverPendingDMs(account, clientHandler());
    }

    public void stop() {
        clientHandler.stop();
    }
}
