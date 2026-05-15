package termchat.command.handlers.message;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;
import termchat.model.ChatRoom;
import termchat.model.User;

import java.io.IOException;

public class DownloadCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {

        if (ctx.requireArgCount(args, 2, ctx, "Usage: /download <file name>")) return;

        User user = ctx.getUser();
        ChatRoom room = user.getActiveChat();
        String filename = args[1];

        try {
            ctx.server().getFileHandler().sendFile(room, user, filename);
        } catch (IOException e) {
            ctx.sendError("An unhandled exception has occurred.");
        }
    }
}
