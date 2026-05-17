package termchat.command.handlers;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;

import static termchat.model.Ansi.*;

public class HelpCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {
        ctx.send(CYAN + "TERMINAL CHAT COMMANDS" + RESET);

        ctx.send(WHITE + "GENERAL\n" + RESET +
            String.format("  %-35s %s%n", "/help", "- displays this help message") +
            String.format("  %-35s %s%n", "/history", "- displays history and search commands") +
            String.format("  %-35s %s%n", "/whoami", "- check who you are logged in as") +
            String.format("  %-35s %s", "/quit", "- stops the application")
        );

        ctx.send(WHITE + "ACCOUNT\n" + RESET +
            String.format("  %-35s %s%n", "/register <username> <password> <email>", "- creates a new account") +
            String.format("  %-35s %s%n", "/login <username> <password>", "- log in to an account") +
            String.format("  %-35s %s", "/logout", "- logs the user out")
        );

        ctx.send(WHITE + "CHAT\n" + RESET +
            String.format("  %-35s %s%n", "/msg <username> <message>", "- sends a private message to a user") +
            String.format("  %-35s %s%n", "/users", "- displays users in the current chatroom (also /u)") +
            String.format("  %-35s %s", "/rooms", "- display the user's chatrooms (also /r)")
        );

        ctx.send(WHITE + "CHATROOM\n" + RESET +
            String.format("  %-35s %s%n", "/createroom <name>", "- creates a new chatroom") +
            String.format("  %-35s %s%n", "/switchroom <chatroom>", "- opens the chatroom (also /s, /switch, /join, /changeroom)") +
            String.format("  %-35s %s%n", "/adduser <username>", "- adds an user to the chatroom") +
            String.format("  %-35s %s%n", "/removeuser <username>", "- removes an user from the chatroom") +
            String.format("  %-35s %s%n", "/changeowner <username>", "- makes an user the new owner of the chatroom") +
            String.format("  %-35s %s%n", "/rename <name>", "- renames the current chatroom") +
            String.format("  %-35s %s", "/leave", "- leaves the current chatroom")
        );
    }
}
