package termchat.command;

import termchat.model.ChatRoom;
import termchat.model.Message;
import termchat.model.User;

import java.io.IOException;
import java.util.*;

import static termchat.model.Ansi.RED;
import static termchat.model.Ansi.RESET;

public class CommandRegistry {
    private final Map<String, CommandHandler> commands = new HashMap<>();
    public CommandRegistry() {
        registerCommands();
    }

    private void registerCommands() {
        commands.put("/register", (args, ctx) -> {
            if (ctx.getUser() != null ) {
                ctx.send(formatError("Error: log out first"));
                return;
            }
            if (args.length != 3) {
                ctx.send(formatError("Usage: register <username> <password>"));
                return;
            }
            String error = ctx.server().registerUser(args[1], args[2]);

            if (error==null) {
                User newAccount = ctx.server().loginUser(args[1], args[2]);
                if (newAccount == null) {
                    ctx.send(formatError("Error: account was created but automatic login failed."));
                    return;
                }

                ctx.setUser(newAccount);
                newAccount.setOnline(true);

                ChatRoom mainChat = ctx.server().RoomManager().getMainChat();
                mainChat.addUser(newAccount);
                newAccount.setActiveChat(mainChat);

                ctx.send("Account registered and logged in as " + newAccount.getUsername());
            } else {
                ctx.send(formatError("Error: " + error));
            }
        });

        commands.put("/login", (args, ctx) -> {
            if (ctx.getUser() != null) {
                ctx.send(formatError("Error: log out first"));
                return;
            }

            if (args.length != 3) {
                ctx.send(formatError("Usage: login <username> <password>"));
                return;
            }

            User found = ctx.server().loginUser(args[1], args[2]);

            // team agreed to not allow multiple accounts logged in at the same time
            if (found.isOnline()) {
                ctx.send(formatError("User has already been logged in."));
            }

            if (found != null) {
                ctx.setUser(found);
                found.setOnline(true);
                ChatRoom mainChat = ctx.server().RoomManager().getMainChat();
                mainChat.addUser(found);
                found.setActiveChat(mainChat);

                ctx.send("Welcome " + found.getUsername());
            } else {
                ctx.send(formatError("Error: invalid username or password"));
            }
        });

        commands.put("/logout", (_, ctx) -> {
            if (ctx.getUser() == null) {
                ctx.send(formatError("You are not logged in"));
                return;
            }
            ctx.send("Logging out, " + ctx.getUser().getUsername());
            ctx.getUser().setOnline(false);
            ctx.setUser(null);

        });

        commands.put("/help", (_, ctx) -> {
            ctx.send("/register <username> <password> - Creates a new account");
            ctx.send("/login <username> <password> - Log in to an account");
            ctx.send("/logout - logs the user out");
            ctx.send("/quit - stops the application");
            ctx.send("/createroom <name> - Creates a new chatroom");
            ctx.send("/switchroom <chatroom> - Opens the chatroom");
            ctx.send("/rooms - display the user's chatrooms");
            ctx.send("/leave - leaves the current chatroom");
            ctx.send("/rename <name> - Renames the current chatroom");
            ctx.send("/adduser <username> - adds an user to the chatroom");
            ctx.send("/removeuser <username> - removes an user from the chatroom");
            ctx.send("/changeowner <username> - makes an user the new owner of the chatroom");
            ctx.send("/history --from <username> --contains <content> --from <start> <end> --limit <number> - displays recent messages from the current chatroom");
            ctx.send("/users - displays users in the current chatroom");
            ctx.send("/msg <username> <message> - sends a private message to a user");
        });

        commands.put("/quit", (_, ctx) -> ctx.stop());

        commands.put("/rooms", (_, ctx) -> {

            User user = ctx.getUser();
            if (user == null) {
                ctx.send(formatError("Log in or register an account first."));
                return;
            }

            List<ChatRoom> chatrooms = ctx.server().RoomManager().getUserChatRooms(user);

            if (chatrooms.isEmpty()) {
                ctx.send(formatError("You are not in any chatrooms."));
            } else {
                ctx.send("Chatrooms You are in:");
                chatrooms.forEach(c -> ctx.send(c.getName()));
            }
        });

        commands.put("/history", (args,ctx) -> {
            if (failedTheUsualChecks(ctx)) return;

            ChatRoom activeChat = ctx.getUser().getActiveChat();
            List<Message> messages = activeChat.getHistory();
            String fromUser = null;
            String containsContent = null;
            Integer rangeStart = null;
            Integer rangeEnd = null;
            int limit = 20; // default limit

            if (messages.isEmpty()) {
                ctx.send(formatError("No messages in this chatroom yet."));
                return;
            }

            for (int i = 1; i < args.length; i++) {
                switch (args[i]) {
                    case "--limit" -> limit = Integer.parseInt(args[++i]);
                    case "--from" -> fromUser = args[++i];
                    case "--contains" -> containsContent = args[++i].toLowerCase();
                    case "--range" -> {
                        try {
                            rangeStart = Integer.parseInt(args[++i]);
                            rangeEnd = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            ctx.send(formatError("Error: Unknown flag arguments, check /help"));
                            return;
                        }
                    }
                    default -> {
                        ctx.send(formatError("Error: Unknown flag, check /help"));
                    }
                }
            }

            // --range filter
            if (rangeStart != null && rangeEnd == null) {
                messages = messages.subList(rangeStart, messages.size());
            } else if (rangeStart != null && rangeEnd != null) {
                messages = messages.subList(rangeStart, rangeEnd);
            } else if (rangeStart == null && rangeEnd != null) {
                messages = messages.subList(0, rangeEnd);
            }

            // then user filter, message containing and limiter
            String finalContainsContent = containsContent;
            String finalFromUser = fromUser;

            List<Message> filtered = messages.stream()
                    .filter(message -> finalFromUser == null || message.getSender().getUsername().equalsIgnoreCase(finalFromUser))
                    .filter(message -> finalContainsContent == null || message.getContent().toLowerCase().contains(finalContainsContent))
                    .limit(limit)
                    .toList();
            if (filtered.isEmpty()) ctx.send(formatError("No messages meet your set flags"));
            filtered.forEach(m -> ctx.send(m.format()));


        });

        commands.put("/users", (_, ctx) -> {
            if (failedTheUsualChecks(ctx)) return;
            ChatRoom activeChat = ctx.getUser().getActiveChat();
            List<User> users = activeChat.getMembers();

            if (users.isEmpty()) {
                ctx.send(formatError("There are no users in this chatroom."));
                return;
            }

            ctx.send("Users in " + activeChat.getName() + ":");

            for (User user : users) {
                ctx.send("– " + user.getUsername());
            }

        });

        commands.put("/msg", (args, ctx) -> {
            if (ctx.getUser() == null) {
                ctx.send(formatError("Log in or register an account first."));
                return;
            }

            if (args.length < 3) {
                ctx.send(formatError("Usage: /msg <username> <message>"));
                return;
            }

            String receiverUSername = args[1];
            String content = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim();

            String error = ctx.server().sendPrivateMessage(ctx.getUser(), receiverUSername, content);
            if (error != null) {
                ctx.send(formatError("ERROR: " + error));
            }
        });

        commands.put("/switchroom", (args, ctx) -> {
           if (args.length < 2) {
               ctx.send(formatError("Usage: /switchroom <chatroom's name>"));
               return;
           }
           User user = ctx.getUser();
           if (user == null) {
               ctx.send(formatError("Log in or register an account first."));
               return;
           }
           String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

           ChatRoom chatroom = ctx.server().RoomManager().getRoom(name, user);
           ctx.server().RoomManager().getMainChat().addUser(user);

           if (chatroom == null) {
               ctx.send(formatError("Chatroom does not exist or you have not been added to it."));
               return;
           }
           user.setActiveChat(chatroom);
        });

        commands.put("/createroom", (args, ctx) -> {

            if (args.length < 2) {
                ctx.send(formatError("/createroom <chatroom's name>"));
                return;
            }

            User user = ctx.getUser();

            if (user == null) {
                ctx.send(formatError("Log in or register an account first."));
                return;
            }
            String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
            String error = ctx.server().RoomManager().createRoom(name, user);
            if (error == null) {
                ctx.send("Chatroom created.");
                ChatRoom chat = ctx.server().RoomManager().getRoom(name, user);
                user.setActiveChat(chat);
                return;
            }
            ctx.send(formatError("ERROR: " + error));
        });

        commands.put("/deleteroom", (_, ctx) -> {

            if (failedTheUsualChecks(ctx)) return;
            User user = ctx.getUser();

            String error = ctx.server().RoomManager().deleteRoom(user.getActiveChat().getName(), user);
            if (error != null) {
                ctx.send(formatError("ERROR: " + error));
            }

            ctx.send("The chatroom has been deleted.");
        });

        commands.put("/rename", (args, ctx) -> {

            if (args.length < 2) {
                ctx.send(formatError("/rename <chatroom's new name>"));
                return;
            }
            if (failedTheUsualChecks(ctx)) {
                return;
            }
            User user = ctx.getUser();

            String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
            String oldName = user.getActiveChat().getName();
            String error = ctx.server().RoomManager().renameRoom(name, user);
            if (error != null) {
                ctx.send(formatError("ERROR: " + error));
                return;
            }

            ctx.server().broadcastSystemMessage(
                    user.getActiveChat(), "Chatroom " + oldName + " was renamed to " + name + "."
            );

            ctx.send("Chatroom successfully renamed to " + name + ".");
        });

        commands.put("/adduser", (args, ctx) -> {
            if (args.length != 2) {
                ctx.send(formatError("/adduser <username>"));
                return;
            }

            if (failedTheUsualChecks(ctx)) {
                return;
            }
            User user = ctx.getUser();

            String error = ctx.server().RoomManager().addUser(user, args[1].trim());
            if (error != null) {
                ctx.send(formatError("ERROR: " + error));
                return;
            }

            ctx.server().broadcastSystemMessage(
                    user.getActiveChat(), args[1].trim() + " was added to the chatroom."
            );

            ctx.send("Successfully added user to chatroom.");
        });

        commands.put("/removeuser", (args, ctx) -> {
            if (args.length != 2) {
                ctx.send(formatError("/removeuser <username>"));
                return;
            }
            if (failedTheUsualChecks(ctx)) {
                return;
            }
            User user = ctx.getUser();

            String error = ctx.server().RoomManager().removeUser(user, args[1].trim());

            if (error != null) {
                ctx.send(formatError("ERROR: " + error));
                return;
            }

            ctx.server().broadcastSystemMessage(
                    user.getActiveChat(), args[1].trim() + " was removed from the chatroom."
            );

            ctx.send("Successfully removed user from chatroom.");
        });

        commands.put("/leave", (_, ctx) -> {
           if (failedTheUsualChecks(ctx)) return;
           User user = ctx.getUser();

           ChatRoom oldChat = user.getActiveChat();

           String error = ctx.server().RoomManager().leaveRoom(user);
           if (error != null) {
               ctx.send(formatError("ERROR: " + error));
               return;
           }

           ctx.server().broadcastSystemMessage(
                   oldChat, user.getUsername() + " has left the chatroom."
           );

           ctx.send("You have left the chatroom.");
        });

        commands.put("/changeowner", (args, ctx) -> {
            if (args.length != 2) {
                ctx.send(formatError("/changeowner <new owner's username>"));
                return;
            }

            if (failedTheUsualChecks(ctx)) {
                return;
            }
            User user = ctx.getUser();

            String error = ctx.server().RoomManager().changeOwner(user, args[1].trim());

            if (error != null) {
                ctx.send(formatError("ERROR: " + error));
                return;
            }
            ctx.send("Succesfully changed the owner.");
        });

        commands.put("/upload", (args, ctx) -> {
            if (args.length != 2) {
                ctx.send("/upload <file path>");
            }
        });

        commands.put("/download", (args, ctx) -> {
            if (args.length != 2) {
                ctx.send("/download <file name>");
                return;
            }

            User user = ctx.getUser();
            ChatRoom room = user.getActiveChat();
            String filename = args[1];
            try {
                ctx.server().FileHandler().sendFile(room, user, filename);
            } catch (IOException e) {
                ctx.send(formatError("An unhandled exception has occurred."));
            }
        });
    }

    public void execute(String input, CommandContext ctx) {
        if (input == null || input.trim().isEmpty()) return;

        String[] args = tokenize(input.trim()).toArray(new String[0]);
        String command = args[0].toLowerCase();

        commands.getOrDefault(command, (_, c) -> {
            if (c.getUser() == null) {
                c.send(formatError("Unknown command. Type /help"));
            } else {
                c.server().routeMessage(input.trim(), c.clientHandler());
            }
        }).handle(args, ctx);
    }

    private boolean failedTheUsualChecks(CommandContext ctx) {
        if (ctx.getUser() == null) {
            ctx.send(formatError("Log in or create an account first."));
            return true;
        }
        if (ctx.getUser().getActiveChat() == null) {
            ctx.send(formatError("You are currently not in a chatroom."));
            return true;
        }
        return false;
    }

    private String formatError(String input) {
        return RED + input + RESET;
    }

    private List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (char c : input.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ' ' && !inQuotes) {
                if (!current.isEmpty()) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }

        if (!current.isEmpty()) {
            tokens.add(current.toString());
        }

        return tokens;
    }
}