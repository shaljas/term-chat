package termchat.command.handlers.chatroom;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;
import termchat.model.ChatRoom;
import termchat.model.User;

public class LeaveRoomCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {
        if (ctx.canExecuteChatroomCommands()) return;

        User user = ctx.getUser();
        ChatRoom oldChat = user.getActiveChat();
        String error = ctx.server().getRoomManager().leaveRoom(user);

        if (error != null) {
            ctx.sendError("Error: " + error);
            return;
        }

        ctx.server().broadcastSystemMessage(
                oldChat, user.getUsername() + " has left the chatroom."
        );

        ctx.send("You have left the chatroom.");
    }
}
