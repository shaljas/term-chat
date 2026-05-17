package termchat.command.handlers.chatroom;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;
import termchat.model.ChatRoom;
import termchat.model.User;

import java.util.List;

import static termchat.model.Ansi.*;

public class UsersCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {
        if (ctx.cannotExecuteChatroomCommands()) return;

        ChatRoom activeChat = ctx.getUser().getActiveChat();

        List<User> users = activeChat.getMembers();
        if (users.isEmpty()) {
            ctx.send("There are no users in this chatroom.");
            return;
        }

        ctx.send(CYAN + "Users in " + BOLD + activeChat.getName() + RESET + CYAN + " are the following:" + RESET);

        for (User user : users) {
            if (ctx.chatRoomFactory().ownerCheck(user, activeChat)) {
                ctx.send(WHITE + String.format("-  %-10s %s", user.getUsername(), "(OWNER)") + RESET);
            } else {
                ctx.send(String.format("-  %-10s", user.getUsername()));
            }
        }

    }
}
