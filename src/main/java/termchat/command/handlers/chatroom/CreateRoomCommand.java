package termchat.command.handlers.chatroom;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;
import termchat.model.ChatRoom;
import termchat.model.User;

import java.util.Arrays;

import static termchat.model.Ansi.*;

public class CreateRoomCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {

        if (ctx.requireArgCount(args, 2, ctx, "Usage: /createroom <chatroom's name>")) return;
        if (ctx.requireLoggedIn()) return;

        User user = ctx.getUser();
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
        String error = ctx.server().getRoomManager().createRoom(name, user);

        if (error == null) {
            ctx.send(CYAN + "Chatroom " + WHITE + name + CYAN + " has been successfully created." + RESET);
            ChatRoom chat = ctx.server().getRoomManager().getRoom(name, user);
            user.setActiveChat(chat);
            return;
        }
        ctx.sendError("Error: " + error);
    }
}
