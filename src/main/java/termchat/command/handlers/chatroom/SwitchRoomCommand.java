package termchat.command.handlers.chatroom;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;
import termchat.model.ChatRoom;
import termchat.model.User;

import java.util.Arrays;

import static termchat.model.Ansi.*;

public class SwitchRoomCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {

        if (ctx.requireArgCount(args, 2, ctx, "Usage: " + args[0] + "<chatroom's name>")) return;
        if (ctx.requireLoggedIn()) return;

        User user = ctx.getUser();
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        ChatRoom chatroom = ctx.chatRoomFactory().getRoom(name, user);
        ctx.chatRoomFactory().getMainChat().addUser(user);

        if (chatroom == null) {
            ctx.sendError("Chatroom does not exist or you have not been added to it.");
            return;
        }
        user.setActiveChat(chatroom);

        new termchat.command.HistoryCommands().history(new String[]{"/history","5"}, ctx);

        ctx.send(CYAN + "You have switched to the " + BOLD + chatroom.getName() + RESET + CYAN + " chatroom." + RESET);
    }
}
