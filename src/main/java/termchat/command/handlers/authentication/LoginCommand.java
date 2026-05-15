package termchat.command.handlers.authentication;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;
import termchat.model.User;

public class LoginCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {
        if (ctx.requireLoggedOut()) return;
        if (ctx.requireArgCount(args, 3, ctx, "Usage: login <username> <password>")) return;

        User account = ctx.loginAndGetAccount(args);
        if (ctx.isAccountInvalid(account, "Invalid username or password.")) return;
        ctx.loginOrRegister("Welcome ", account);
    }
}
