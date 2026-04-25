package termchat.command;

import termchat.model.ChatRoom;
import termchat.model.User;

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
                ctx.send("Account registered");
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
                ctx.send("Welcome " + found.getUsername());
            } else {
                ctx.send("Error: invalid username or password");
            }
        });

        commands.put("/logout", (args, ctx) -> {
            if (ctx.getUser() == null) {
                ctx.send("You are not logged in");
                return;
            }
            ctx.send("Logging out, " + ctx.getUser().getUsername());
            ctx.getUser().setOnline(false);
            ctx.setUser(null);

        });

        commands.put("/help", (args, ctx) -> {
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
        });

        commands.put("/quit", (args, ctx) -> ctx.stop());

        commands.put("/rooms", (args, ctx) -> {

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

           if (chatroom == null) {
               ctx.send("Chatroom does not exist or you have not been added to it.");
               return;
           }
           user.setActiveChat(chatroom);
           ctx.send("Switched to " + chatroom.getName());
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

        commands.put("/deleteroom", (args, ctx) -> {

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
            String error = ctx.server().RoomManager().renameRoom(name, user);
            if (error != null) {
                ctx.send("ERROR: " + error);
                return;
            }
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
            ctx.send("Successfully added user to chatroom.");
        });

        commands.put("/removeuser", (args, ctx) -> {
            if (args.length != 2) {
                ctx.send("/removeuser <username>");
                return;
            }
            if (failedTheUsualChecks(ctx)) {
                return;
            };
            User user = ctx.getUser();

            String error = ctx.server().RoomManager().removeUser(user, args[1].trim());

            if (error != null) {
                ctx.send("ERROR: " + error);
                return;
            }
            ctx.send("Successfully removed user from chatroom.");
        });

        commands.put("/leave", (args, ctx) -> {
           if (failedTheUsualChecks(ctx)) return;
           User user = ctx.getUser();

           String error = ctx.server().RoomManager().leaveRoom(user);
           if (error != null) {
               ctx.send("ERROR: " + error);
               return;
           }
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
    }

    public void execute(String input, CommandContext ctx) {
        String[] args = input.split(" ");
        String command = args[0].toLowerCase();

        commands.getOrDefault(command, (a, c) -> {
            if (c.getUser() == null) {
                c.send("Unknown command. Type /help");
            } else {
                c.server().routeMessage(input, c.clientHandler());
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