package termchat.command.handlers;

import termchat.command.CommandContext;
import termchat.command.CommandHandler;

public class HelpCommand implements CommandHandler {
    @Override
    public void handle(String[] args, CommandContext ctx) {
        ctx.send("Terminal chat commands:");
        ctx.send("\t/register <username> <password> - Creates a new account");
        ctx.send("\t/login <username> <password> - Log in to an account");
        ctx.send("\t/logout - logs the user out");
        ctx.send("\t/quit - stops the application");
        ctx.send("\t/createroom <name> - Creates a new chatroom");
        ctx.send("\t/switchroom <chatroom> - Opens the chatroom");
        ctx.send("\t/rooms - display the user's chatrooms");
        ctx.send("\t/leave - leaves the current chatroom");
        ctx.send("\t/rename <name> - Renames the current chatroom");
        ctx.send("\t/adduser <username> - adds an user to the chatroom");
        ctx.send("\t/removeuser <username> - removes an user from the chatroom");
        ctx.send("\t/changeowner <username> - makes an user the new owner of the chatroom");
        ctx.send("\t/history --from <username> --contains <content> --from <start> <end> --limit <number> - displays recent messages from the current chatroom");
        ctx.send("\t/users - displays users in the current chatroom");
        ctx.send("\t/msg <username> <message> - sends a private message to a user");
    }
}
