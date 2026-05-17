package termchat.command.handlers;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;

import static termchat.model.Ansi.*;

public class WhoAmICommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {
        if (ctx.requireLoggedIn()) return;
        ctx.send(CYAN + "You are logged in as " + BOLD +  ctx.getUser().getUsername() +
                RESET + CYAN + ".");
    }
}
