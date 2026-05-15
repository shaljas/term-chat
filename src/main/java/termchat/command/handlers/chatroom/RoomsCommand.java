package termchat.command.handlers.chatroom;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;
import termchat.model.ChatRoom;
import termchat.model.User;

import java.util.List;

import static termchat.model.Ansi.*;

public class RoomsCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {

        if (ctx.requireLoggedIn()) return;
        User user = ctx.getUser();

        List<ChatRoom> chatrooms = ctx.server().getRoomManager().getUserChatRooms(user);

        if (chatrooms.isEmpty()) {
            ctx.sendError("You are not in any chatrooms.");
            return;
        }

        ctx.send(CYAN + "You are in the following chatrooms:" + RESET);
        ChatRoom activeRoom = ctx.getUser().getActiveChat();

        for (ChatRoom chatroom : chatrooms) {
            if (chatroom == activeRoom) {
                ctx.send(WHITE + String.format("-  %-10s %s", chatroom.getName(), "(ACTIVE)") + RESET);
            } else {
                ctx.send(String.format("-  %-10s", chatroom.getName()));
            }
        }
    }
}
