package termchat.command.handlers.message;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;

import java.util.Arrays;

public class MessageCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {

        if (args.length < 3) {
            ctx.sendError("Usage: /msg <username> <message>");
            return;
        }

        if (ctx.requireLoggedIn()) return;

        String receiverUsername = args[1];
        String content = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim();

        String error = ctx.messageRouter().sendPrivateMessage(ctx.getUser(), receiverUsername, content);

        if (error != null) {
            ctx.sendError("Error: " + error);
        }
    }
}
