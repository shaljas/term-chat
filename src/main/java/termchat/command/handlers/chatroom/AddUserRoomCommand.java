package termchat.command.handlers.chatroom;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;
import termchat.model.User;

import static termchat.model.Ansi.*;

public class AddUserRoomCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {

        if (ctx.requireArgCount(args, 2, ctx, "Usage: /adduser <username>")) return;
        if (ctx.cannotExecuteChatroomCommands()) return;

        User user = ctx.getUser();
        String error = ctx.server().getRoomManager().addUser(user, args[1].trim());

        if (error != null) {
            ctx.sendError("Error: " + error);
            return;
        }

        ctx.server().broadcastSystemMessage(
                user.getActiveChat(),
                args[1].trim() + " was added to the " +
                        BOLD + user.getActiveChat().getName() + RESET + YELLOW + " chatroom."
        );
    }
}
