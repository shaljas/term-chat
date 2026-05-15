package termchat.command.handlers.authentication;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;

public class LogoutCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {
        if (ctx.requireLoggedIn()) return;

        ctx.send("Logging out, " + ctx.getUser().getUsername());
        ctx.getUser().setOnline(false);
        ctx.setUser(null);
    }
}
