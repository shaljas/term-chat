package termchat.command.handlers.chatroom;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;
import termchat.model.ChatRoom;
import termchat.model.User;

import java.util.List;

import static termchat.model.Ansi.MAGENTA;
import static termchat.model.Ansi.RESET;

public class UsersCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {
        if (ctx.canExecuteChatroomCommands()) return;

        ChatRoom activeChat = ctx.getUser().getActiveChat();

        List<User> users = activeChat.getMembers();
        if (users.isEmpty()) {
            ctx.send("There are no users in this chatroom.");
            return;
        }

        ctx.send("Users in " + activeChat.getName() + ":");

        for (User user : users) {
            if (ctx.server().getRoomManager().ownerCheck(user, activeChat)) {
                ctx.send(MAGENTA + "– " + user.getUsername() + " (owner)" + RESET);
            } else {
                ctx.send("– " + user.getUsername());
            }
        }

    }
}
