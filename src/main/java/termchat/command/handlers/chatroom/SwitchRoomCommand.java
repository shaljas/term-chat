package termchat.command.handlers.chatroom;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;
import termchat.model.ChatRoom;
import termchat.model.User;

import java.util.Arrays;

public class SwitchRoomCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {

        if (ctx.requireArgCount(args, 2, ctx, "Usage: /switchroom <chatroom's name>")) return;
        if (ctx.requireLoggedIn()) return;

        User user = ctx.getUser();
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        ChatRoom chatroom = ctx.server().getRoomManager().getRoom(name, user);
        ctx.server().getRoomManager().getMainChat().addUser(user);

        if (chatroom == null) {
            ctx.sendError("Chatroom does not exist or you have not been added to it.");
            return;
        }
        user.setActiveChat(chatroom);

        ctx.send("You have switched to the \"" + chatroom.getName() + "\" chatroom.");
    }
}
