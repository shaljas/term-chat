package termchat.command.handlers.chatroom;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;
import termchat.model.User;

import static termchat.model.Ansi.*;

public class RemoveUserRoomCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {

        if (ctx.requireArgCount(args, 2, ctx, "Usage: /removeuser <username>")) return;
        if (ctx.cannotExecuteChatroomCommands()) return;

        User user = ctx.getUser();
        String error = ctx.server().getRoomManager().removeUser(user, args[1].trim());

        if (error != null) {
            ctx.sendError("Error: " + error);
            return;
        }

        ctx.server().broadcastSystemMessage(
                user.getActiveChat(), BOLD + args[1].trim() + RESET + YELLOW +
                        " has been removed from the " + BOLD + user.getActiveChat().getName() +
                        RESET + YELLOW + " chatroom." + RESET
        );
    }
}
