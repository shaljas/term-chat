package termchat.command.handlers.chatroom;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;
import termchat.model.User;

import static termchat.model.Ansi.CYAN;
import static termchat.model.Ansi.RESET;

public class DeleteRoomCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {
        if (ctx.cannotExecuteChatroomCommands()) return;
        User user = ctx.getUser();

        String error = ctx.server().getRoomManager().deleteRoom(user.getActiveChat().getName(), user);

        if (error != null) {
            ctx.sendError("Error: " + error);
            return;
        }

        ctx.send(CYAN + "The chatroom has been deleted." + RESET);
    }
}
