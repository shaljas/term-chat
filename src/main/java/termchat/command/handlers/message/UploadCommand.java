package termchat.command.handlers.message;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;

public class UploadCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {
        if (ctx.requireArgCount(args, 2, ctx, "Usage: /upload <file path>"));
    }
}
