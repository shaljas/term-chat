package termchat.command.handlers.authentication;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;
import termchat.model.User;

import static termchat.model.Ansi.CYAN;
import static termchat.model.Ansi.RESET;

public class LoginCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {
        if (ctx.requireLoggedOut()) return;
        if (ctx.requireArgCount(args, 3, ctx, "Usage: login <username> <password>")) return;

        User account = ctx.loginAndGetAccount(args);
        if (ctx.isAccountInvalid(account, "Invalid username or password.")) return;

        ctx.loginOrRegister(CYAN + "Login successful! Welcome " + RESET, account);
    }
}
