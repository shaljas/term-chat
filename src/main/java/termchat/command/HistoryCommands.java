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

        if (args.length >= 2 && args[1].equalsIgnoreCase("help")) {
            showHistoryHelp(ctx);
            return;
        }

        if (args.length >= 2 && args[1].equalsIgnoreCase("from")) {
            showMessagesFromUser(args, ctx, messages);
            return;
        }

        if (args.length >= 2 && args[1].equalsIgnoreCase("search")) {
            showMessagesContaining(args, ctx, messages);
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

    private void showHistoryHelp(CommandContext ctx) {
        ctx.send("Chat history commands: ");
        ctx.send("/history - displays recent messages from the current chatroom");
        ctx.send("/history <number> - displays the latest number of messages");
        ctx.send("/history from <username> - displays messages from a user");
        ctx.send("/history from <username> <number> - displays latest messages from a user");
        ctx.send("/history search <keyword> - searches messages by keyword");
        ctx.send("/history search <keyword> <number> - searches latest messages by keyword");
    }

    private void showMessagesContaining(String[] args, CommandContext ctx, List<Message> messages) {
        if (args.length != 3 && args.length != 4) {
            ctx.send("Usage: /history from <keyword> <number>");
            return;
        }

        String keyword = args[2];
        int limit = 20;

        if (args.length == 4) {
            try {
                limit = Integer.parseInt(args[3]);
                if (limit <= 0) {
                    ctx.send("History limit must be a positive number.");
                    return;
                }
            } catch (NumberFormatException e) {
                ctx.send("Usage: /history search <keyword> <number>");
                return;
            }
        }

        List<Message> filteredMessages = messages.stream().filter(message -> message.getContent().toLowerCase().contains(keyword.toLowerCase())).toList();

        if (filteredMessages.isEmpty()) {
            ctx.send("No messages containing \"" + keyword + "\" in this chatroom.");
            return;
        }

        int start = Math.max(0, filteredMessages.size()-limit);
        for (int i = start; i < filteredMessages.size(); i++) {
            ctx.send(filteredMessages.get(i).format());
        }


    }

    private void showMessagesFromUser(String[] args, CommandContext ctx, List<Message> messages) {
        if (args.length != 3 && args.length != 4) {
            ctx.send("Usage: /history from <username> <number>");
            return;
        }

        String username = args[2];
        int limit = 20;

        if (args.length == 4) {
            try {
                limit = Integer.parseInt(args[3]);
                if (limit <= 0) {
                    ctx.send("History limit must be a positive number.");
                    return;
                }
            } catch (NumberFormatException e) {
                ctx.send("Usage: /history from <username> <number>");
                return;
            }
        }

        List<Message> filteredMessages = messages.stream().filter(message -> message.getSender().getUsername().equalsIgnoreCase(username)).toList();

        if (filteredMessages.isEmpty()) {
            ctx.send("No messages from " + username + " in this chatroom.");
            return;
        }

        int start = Math.max(0, filteredMessages.size()-limit);
        for (int i = start; i < filteredMessages.size(); i++) {
            ctx.send(filteredMessages.get(i).format());
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
