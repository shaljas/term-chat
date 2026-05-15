package termchat.command.handlers.chatroom;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;
import termchat.model.User;

import java.util.Arrays;

public class RenameRoomCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {

        if (ctx.requireArgCount(args, 2, ctx, "Usage: /rename <chatroom's new name>")) return;

        if (ctx.canExecuteChatroomCommands()) return;
        User user = ctx.getUser();

        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
        String oldName = user.getActiveChat().getName();
        String error = ctx.server().getRoomManager().renameRoom(name, user);

        if (error != null) {
            ctx.sendError("Error: " + error);
            return;
        }

        ctx.server().broadcastSystemMessage(
                user.getActiveChat(), "Chatroom \"" + oldName + "\" was renamed to \"" + name + "\"."
        );
    }
}
