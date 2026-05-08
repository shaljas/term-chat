package termchat.command;

import termchat.model.ChatRoom;
import termchat.model.Message;

import java.util.List;

public class HistoryCommands {
    public void history(String[] args, CommandContext ctx) {
        if (failedTheUsualChecks(ctx)) return;

        ChatRoom activeChat = ctx.getUser().getActiveChat();
        List<Message> messages = activeChat.getHistory();

        if (messages.isEmpty()) {
            ctx.send("No messages in this chatroom yet.");
            return;
        }

        int limit = 20;
        if (args.length == 2) {
            try {
                limit = Integer.parseInt(args[1]);

                if (limit <= 0) {
                    ctx.send("History limit must be a positive number.");
                    return;
                }
            } catch (NumberFormatException e) {
                ctx.send("Usage: /history <number>");
                return;
            }
        } else if (args.length > 2) {
            ctx.send("Usage: /history <number>");
            return;
        }

        int start = Math.max(0, messages.size() - limit);
        for (int i = start; i < messages.size(); i++) {
            ctx.send(messages.get(i).format());
        }
    }

    private boolean failedTheUsualChecks(CommandContext ctx) {
        if (ctx.getUser() == null) {
            ctx.send("Log in or create an account first.");
            return true;
        }
        if (ctx.getUser().getActiveChat() == null) {
            ctx.send("You are currently not in a chatroom.");
            return true;
        }
        return false;
    }
}
