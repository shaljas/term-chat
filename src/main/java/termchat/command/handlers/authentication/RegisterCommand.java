package termchat.command.handlers.authentication;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;
import termchat.exceptions.UsernameTakenException;
import termchat.model.User;

public class RegisterCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {
        if (ctx.requireLoggedOut()) return;
        if (ctx.requireArgCount(args, 4, ctx, "Usage: /register <username> <password> <email>")) return;

        try {
            ctx.server().registerUser(args[1], args[2], args[3]);
        } catch (UsernameTakenException e) {
            ctx.sendError("Error: username already taken");
            return;
        } catch (Exception e) {
            ctx.sendError("Error: registration failed");
            return;
        }

        User newAccount = ctx.loginAndGetAccount(args);

        if (ctx.isAccountInvalid(newAccount,
                "Account was created but automatic login failed.")) return;

        ctx.loginOrRegister("Account registered and logged in as ", newAccount);
    }
}
