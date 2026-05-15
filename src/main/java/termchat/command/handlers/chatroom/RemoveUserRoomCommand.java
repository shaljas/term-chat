package termchat.command.handlers.chatroom;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;
import termchat.model.User;

public class RemoveUserRoomCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {

        if (ctx.requireArgCount(args, 2, ctx, "Usage: /removeuser <username>")) return;
        if (ctx.canExecuteChatroomCommands()) return;

        User user = ctx.getUser();
        String error = ctx.server().getRoomManager().removeUser(user, args[1].trim());

        if (error != null) {
            ctx.sendError("Error: " + error);
            return;
        }

        ctx.server().broadcastSystemMessage(
                user.getActiveChat(), args[1].trim() + " was removed from the \"" + user.getActiveChat() + "\" chatroom."
        );
    }
}
