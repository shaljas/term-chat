package termchat.command;

import termchat.model.ChatRoom;
import termchat.model.Message;
import termchat.model.User;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandRegistry {
    private final Map<String, CommandHandler> commands = new HashMap<>();
    public CommandRegistry() {
        registerCommands();
    }

    private void registerCommands() {
        commands.put("/register", (args, ctx) -> {
            if (ctx.getUser() != null ) {
                ctx.send("Error: log out first");
                return;
            }
            if (args.length != 3) {
                ctx.send("Usage: register <username> <password>");
                return;
            }
            String error = ctx.server().registerUser(args[1], args[2]);

            if (error==null) {
                User newAccount = ctx.server().loginUser(args[1], args[2]);
                if (newAccount == null) {
                    ctx.send("Error: account was created but automatic login failed.");
                    return;
                }

                ctx.setUser(newAccount);
                newAccount.setOnline(true);

                ChatRoom mainChat = ctx.server().RoomManager().getMainChat();
                mainChat.addUser(newAccount);
                newAccount.setActiveChat(mainChat);

                ctx.send("Account registered and logged in as " + newAccount.getUsername());
            } else {
                ctx.send("Error: " + error);
            }
        });

        commands.put("/login", (args, ctx) -> {
            if (ctx.getUser() != null) {
                ctx.send("Error: log out first");
                return;
            }

            if (args.length != 3) {
                ctx.send("Usage: login <username> <password>");
                return;
            }

            User found = ctx.server().loginUser(args[1], args[2]);

            if (found != null) {
                ctx.setUser(found);
                found.setOnline(true);
                ChatRoom mainChat = ctx.server().RoomManager().getMainChat();
                mainChat.addUser(found);
                found.setActiveChat(mainChat);

                ctx.send("Welcome " + found.getUsername());
            } else {
                ctx.send("Error: invalid username or password");
            }
        });

        commands.put("/logout", (_, ctx) -> {
            if (ctx.getUser() == null) {
                ctx.send("You are not logged in");
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
            ctx.send("/history <number> - displays recent messages from the current chatroom");
            ctx.send("/users - displays users in the current chatroom");
            ctx.send("/msg <username> <message> - sends a private message to a user");
        });

        commands.put("/quit", (_, ctx) -> ctx.stop());

        commands.put("/rooms", (_, ctx) -> {

            User user = ctx.getUser();
            if (user == null) {
                ctx.send("Log in or register an account first.");
                return;
            }

            List<ChatRoom> chatrooms = ctx.server().RoomManager().getUserChatRooms(user);

            if (chatrooms.isEmpty()) {
                ctx.send("You are not in any chatrooms.");
            } else {
                ctx.send("Chatrooms You are in:");
                chatrooms.forEach(c -> ctx.send(c.getName()));
            }
        });

        commands.put("/history", (args,ctx) -> {
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
        });

        commands.put("/users", (_, ctx) -> {
            if (failedTheUsualChecks(ctx)) return;
            ChatRoom activeChat = ctx.getUser().getActiveChat();
            List<User> users = activeChat.getMembers();

            if (users.isEmpty()) {
                ctx.send("There are no users in this chatroom.");
                return;
            }

            ctx.send("Users in " + activeChat.getName() + ":");

            for (User user : users) {
                ctx.send("– " + user.getUsername());
            }

        });

        commands.put("/msg", (args, ctx) -> {
            if (ctx.getUser() == null) {
                ctx.send("Log in or register an account first.");
                return;
            }

            if (args.length < 3) {
                ctx.send("Usage: /msg <username> <message>");
                return;
            }

            String receiverUSername = args[1];
            String content = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim();

            String error = ctx.server().sendPrivateMessage(ctx.getUser(), receiverUSername, content);
            if (error != null) {
                ctx.send("ERROR: " + error);
            }
        });

        commands.put("/switchroom", (args, ctx) -> {
           if (args.length < 2) {
               ctx.send("/switchroom <chatroom's name>");
               return;
           }
           User user = ctx.getUser();
           if (user == null) {
               ctx.send("Log in or register an account first.");
               return;
           }
           String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

           ChatRoom chatroom = ctx.server().RoomManager().getRoom(name, user);
           ctx.server().RoomManager().getMainChat().addUser(user);

           if (chatroom == null) {
               ctx.send("Chatroom does not exist or you have not been added to it.");
               return;
           }
           user.setActiveChat(chatroom);
        });

        commands.put("/createroom", (args, ctx) -> {

            if (args.length < 2) {
                ctx.send("/createroom <chatroom's name>");
                return;
            }

            User user = ctx.getUser();

            if (user == null) {
                ctx.send("Log in or register an account first.");
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
            ctx.send("ERROR: " + error);
        });

        commands.put("/deleteroom", (_, ctx) -> {

            if (failedTheUsualChecks(ctx)) return;
            User user = ctx.getUser();

            String error = ctx.server().RoomManager().deleteRoom(user.getActiveChat().getName(), user);
            if (error != null) {
                ctx.send("ERROR: " + error);
            }

            ctx.send("The chatroom has been deleted.");
        });

        commands.put("/rename", (args, ctx) -> {

            if (args.length < 2) {
                ctx.send("/rename <chatroom's new name>");
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
                ctx.send("ERROR: " + error);
                return;
            }

            ctx.server().broadcastSystemMessage(
                    user.getActiveChat(), "Chatroom " + oldName + " was renamed to " + name + "."
            );

            ctx.send("Chatroom successfully renamed to " + name + ".");
        });

        commands.put("/adduser", (args, ctx) -> {
            if (args.length != 2) {
                ctx.send("/adduser <username>");
                return;
            }

            if (failedTheUsualChecks(ctx)) {
                return;
            }
            User user = ctx.getUser();

            String error = ctx.server().RoomManager().addUser(user, args[1].trim());
            if (error != null) {
                ctx.send("ERROR: " + error);
                return;
            }

            ctx.server().broadcastSystemMessage(
                    user.getActiveChat(), args[1].trim() + " was added to the chatroom."
            );

            ctx.send("Successfully added user to chatroom.");
        });

        commands.put("/removeuser", (args, ctx) -> {
            if (args.length != 2) {
                ctx.send("/removeuser <username>");
                return;
            }
            if (failedTheUsualChecks(ctx)) {
                return;
            }
            User user = ctx.getUser();

            String error = ctx.server().RoomManager().removeUser(user, args[1].trim());

            if (error != null) {
                ctx.send("ERROR: " + error);
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
               ctx.send("ERROR: " + error);
               return;
           }

           ctx.server().broadcastSystemMessage(
                   oldChat, user.getUsername() + " has left the chatroom."
           );

           ctx.send("You have left the chatroom.");
        });

        commands.put("/changeowner", (args, ctx) -> {
            if (args.length != 2) {
                ctx.send("/changeowner <new owner's username>");
                return;
            }

            if (failedTheUsualChecks(ctx)) {
                return;
            }
            User user = ctx.getUser();

            String error = ctx.server().RoomManager().changeOwner(user, args[1].trim());

            if (error != null) {
                ctx.send("ERROR: " + error);
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
                ctx.send("An unhandled exception has occurred.");
            }
        });
    }

    public void execute(String input, CommandContext ctx) {
        if (input == null || input.trim().isEmpty()) return;

        String[] args = input.trim().split(" ");
        String command = args[0].toLowerCase();

        commands.getOrDefault(command, (_, c) -> {
            if (c.getUser() == null) {
                c.send("Unknown command. Type /help");
            } else {
                c.server().routeMessage(input.trim(), c.clientHandler());
            }
        }).handle(args, ctx);
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