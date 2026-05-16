package termchat.command.handlers.authentication;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;
import termchat.exceptions.UsernameTakenException;
import termchat.model.User;

import static termchat.model.Ansi.CYAN;
import static termchat.model.Ansi.RESET;

public class RegisterCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {
        if (ctx.requireLoggedOut()) return;
        if (ctx.requireArgCount(args, 4, ctx, "Usage: /register <username> <password> <email>")) return;

        try {
            ctx.authService().registerUser(args[1], args[2], args[3]);
        } catch (UsernameTakenException e) {
            ctx.sendError(e.getMessage());
            return;
        } catch (Exception e) {
            ctx.sendError("Error: registration failed");
            return;
        }

        String username = args[1];
        String password = args[2];

        User newAccount = ctx.authService().loginUser(username, password);

        if (ctx.isAccountInvalid(newAccount,
                "Account was created but automatic login failed.")) return;

        ctx.loginOrRegister(CYAN + "Account registered and logged in as " + RESET, newAccount);
    }
}
