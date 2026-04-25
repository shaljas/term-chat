package termchat.service;

import termchat.model.ChatRoom;
import termchat.model.User;

import java.util.Arrays;
import java.util.HashMap;
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
            ctx.send("/leave - leaves the current chatroom");
            ctx.send("/rename <name> - Renames the current chatroom");
            ctx.send("/adduser <username> - adds an user to the chatroom");
            ctx.send("/removeuser <username> - removes an user from the chatroom");
            ctx.send("/changeowner <username> - makes an user the new owner of the chatroom");
        });

        commands.put("/quit", (args, ctx) -> ctx.stop());

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
           ChatRoom chat = user.getChatrooms()
                   .stream()
                   .filter(c -> c.getName().equalsIgnoreCase(name))
                   .findFirst().orElse(null);
           if (chat == null) {
               ctx.send("Chatroom does not exist or you have not been added to it.");
               return;
           }
           user.setActiveChat(chat);
           ctx.send("Switched to " + chat.getName());
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
            String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            try {
                synchronized (this) {
                    if (ctx.server().getChatRooms().stream().anyMatch(c -> c.getName().equalsIgnoreCase(name))) {
                        ctx.send("Chatroom with that name exists already.");
                        return;
                    }
                    int control = ctx.server().getChatRooms().size();
                    ctx.server().createRoom(name, user);
                    if (ctx.server().getChatRooms().size() == control) {
                        throw new Exception();
                    }
                }
                ctx.send("Room " + name + " created sucessfully.");
            } catch (Exception e) {
                ctx.send("Could not create a chatroom.");
            }
        });

        commands.put("/deleteroom", (args, ctx) -> {

            if (failedTheUsualChecks(ctx)) return;
            User user = ctx.getUser();
            ChatRoom chat = user.getActiveChat();

            if (chat.getOwner() != user) {
                ctx.send("Only the owner can use this command.");
                return;
            }

            ctx.server().deleteroom(chat);
            if (ctx.server().getChatRooms().contains(chat)) {
                ctx.send("Something went wrong. The chatroom has not been deleted.");
                return;
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
            ChatRoom chat = user.getActiveChat();

            if (chat.getOwner() != user) {
                ctx.send("Only the owner can use this command.");
                return;
            }

            String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            chat.rename(name, user);
            if (chat.getName().equals(name)) {
                ctx.send("Chatroom succesfully renamed to " + name + ".");
                return;
            }
            ctx.send("Could not rename the chatroom.");
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
            ChatRoom chat = user.getActiveChat();

            if (chat.getOwner() != user) {
                ctx.send("Only the owner can use this command.");
                return;
            }

            User userToBeAdded = ctx.server().getUserRepository().findByUsername(args[1]).orElse(null);
            if (userToBeAdded == null) {
                ctx.send("Could not find user " + args[1] + ".");
                return;
            }
            if (chat.getUserByName(args[1]) != null) {
                ctx.send("User is already in the chatroom.");
                return;
            }
            chat.addUser(userToBeAdded);
            userToBeAdded.getChatrooms().add(chat);
            if (chat.getUserByName(args[1]) != null) {
                ctx.send(userToBeAdded.getUsername() + " has been added to the chatroom.");
                return;
            }
            ctx.send("Could not add the user.");
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
            ChatRoom chat = user.getActiveChat();
            User userToBeRemoved = chat.getUserByName(args[1]);

            if (user == userToBeRemoved) {
                ctx.send("You cannot remove yourself! Use /leave instead.");
                return;
            }

            if (chat.getOwner() != user) {
                ctx.send("Only the owner can use this command.");
                return;
            }

            if (userToBeRemoved == null) {
                ctx.send("Could not find the user from this chatroom.");
                return;
            }
            chat.removeUser(userToBeRemoved);
            userToBeRemoved.setActiveChat(null);
            userToBeRemoved.getChatrooms().remove(chat);
            if (chat.getUserByName(args[1]) == null) {
                ctx.send(userToBeRemoved.getUsername() + " has been removed from the chatroom");
                return;
            }
            ctx.send("Could not remove the user.");
        });

        commands.put("/leave", (args, ctx) -> {
           if (failedTheUsualChecks(ctx)) return;
           User user = ctx.getUser();
           ChatRoom chat = user.getActiveChat();
            if (chat.getParticipants().size() > 1 && user == chat.getOwner()) {
                ctx.send("You cannot leave as the owner while there are other members in the chatroom.");
                ctx.send("You can:");
                ctx.send("A: Appoint another member as the chatroom's owner and try again.");
                ctx.send("B: Kick all other members first and try again.");
                ctx.send("C: Use /deleteroom;");
                return;
            }
           chat.removeUser(user);
           user.setActiveChat(null);
           user.getChatrooms().remove(chat);
           if (chat.getParticipants().isEmpty()) {
               ctx.server().deleteroom(chat);
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
            ChatRoom chat = user.getActiveChat();

            if (chat.getOwner() != user) {
                ctx.send("Only the owner can use this command.");
                return;
            }

            chat.changeowner(args[1], user);

            if (chat.getOwner() != user) {
                ctx.send("Owner successfully changed to " + chat.getOwner().getUsername());
                return;
            }
            ctx.send("Could not change the owner.");
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