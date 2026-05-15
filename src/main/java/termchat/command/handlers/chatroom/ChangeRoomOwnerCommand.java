package termchat.command.handlers.chatroom;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;
import termchat.model.User;

public class ChangeRoomOwnerCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {

        if(ctx.requireArgCount(args, 2, ctx, "Usage: /changeowner <new owner's username>")) return;
        if (ctx.canExecuteChatroomCommands()) return;

        User user = ctx.getUser();
        String error = ctx.server().getRoomManager().changeOwner(user, args[1].trim());

        if (error != null) {
            ctx.sendError("Error: " + error);
            return;
        }

        ctx.send("Successfully changed the owner.");
    }
}
