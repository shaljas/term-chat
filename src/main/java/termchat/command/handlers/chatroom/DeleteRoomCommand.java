package termchat.command.handlers.chatroom;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;
import termchat.model.User;

import java.io.IOException;

import static termchat.model.Ansi.*;

public class DeleteRoomCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {
        if (ctx.cannotExecuteChatroomCommands()) return;
        User user = ctx.getUser();


        if (!ctx.getUserConfirmation("Confirm room deletion by typing room \""
                + BOLD + user.getActiveChat().getName() + RESET + CYAN + "\" name:",
                user.getActiveChat().getName())
        ) return;


        String error = ctx.server().getRoomManager().deleteRoom(user.getActiveChat().getName(), user);

        if (error != null) {
            ctx.sendError("Error: " + error);
            return;
        }

        ctx.send(CYAN + "The chatroom has been deleted." + RESET);
    }
}
