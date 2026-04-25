package termchat.service;

import termchat.model.ChatRoom;
import termchat.model.User;

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

        });

        commands.put("/quit", (args, ctx) -> ctx.stop());

        commands.put("/createroom", (args, ctx) -> {
            int control = ctx.server().getRoomsSize();
            ctx.server().createRoom(args[1], ctx.getUser());
            if (ctx.server().getRoomsSize() > control) {
                ctx.send("Room " + args[1] + " created successfully.");
            } else {
                ctx.send("Something went wrong. Please try again.");
            }
        });

        commands.put("/rename", (args, ctx) -> {
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
}