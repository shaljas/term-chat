package termchat.command;

import termchat.client.ClientHandler;
import termchat.client.OutputChannel;
import termchat.model.ChatRoom;
import termchat.model.User;
import termchat.server.ChatRoomFactory;
import termchat.server.FileTransfer;
import termchat.server.MessageRouter;
import termchat.service.AuthService;

import java.io.IOException;

import static termchat.model.Ansi.*;

public record CommandContext(
        OutputChannel outputChannel,
        AuthService authService,
        ChatRoomFactory chatRoomFactory,
        MessageRouter messageRouter,
        FileTransfer fileTransfer) {

    public User getUser() {
        return outputChannel.getUser();
    }

    public void setUser(User user) {
        outputChannel.setUser(user);
    }

    public void send(String message) {
        outputChannel.sendToClient(message);
    }

    public void sendError(String message) {
        outputChannel.sendToClient(RED + message + RESET);
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

        ChatRoom mainChat = chatRoomFactory.getMainChat();
        mainChat.addUser(account);
        account.setActiveChat(mainChat);

        new termchat.command.HistoryCommands().history(new String[]{"/history","5"}, this);
        send(message + account.getUsername());
        messageRouter().deliverPendingDMs(account, outputChannel);
    }

    public boolean getUserConfirmation(String confirmQuestion, String confirmText) {
        send(CYAN + confirmQuestion + RESET);

        try {
            String messageIn = outputChannel.readStringInput();
            return messageIn.equalsIgnoreCase(confirmText);
        } catch (IOException e) {
            sendError("Error: unable to read confirmation. Try again.");
            return false;
        }
    }

    public void stop() {
        outputChannel.stop();
    }
}
