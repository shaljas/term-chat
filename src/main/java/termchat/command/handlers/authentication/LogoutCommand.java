package termchat.command.handlers.authentication;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;

import static termchat.model.Ansi.*;

public class LogoutCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {
        if (ctx.requireLoggedIn()) return;

        ctx.send(CYAN + "Logged out. See you soon " + BOLD + ctx.getUser().getUsername() + RESET + CYAN + "!" + RESET);
        ctx.getUser().setOnline(false);
        ctx.setUser(null);
    }
}
