package termchat.command.handlers.chatroom;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;
import termchat.model.User;

import static termchat.model.Ansi.*;

public class ChangeRoomOwnerCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {

        if(ctx.requireArgCount(args, 2, ctx, "Usage: /changeowner <new owner's username>")) return;
        if (ctx.cannotExecuteChatroomCommands()) return;

        User user = ctx.getUser();

        if (!ctx.getUserConfirmation("Confirm owner transfer by typing your \""
                        + BOLD + user.getUsername() + RESET + CYAN + "\" username:",
                user.getUsername())
        ) return;

        String error = ctx.server().getRoomManager().changeOwner(user, args[1].trim());

        if (error != null) {
            ctx.sendError("Error: " + error);
            return;
        }

        ctx.server().broadcastSystemMessage(
                user.getActiveChat(),
                args[1].trim() + " is the new owner of the " +
                        BOLD + user.getActiveChat().getName() + RESET + YELLOW + " chatroom."
        );
        ctx.send(CYAN + "Successfully changed the owner." + RESET);
    }
}
