package termchat.command;

import termchat.command.handlers.*;
import termchat.command.handlers.authentication.LoginCommand;
import termchat.command.handlers.authentication.LogoutCommand;
import termchat.command.handlers.authentication.RegisterCommand;
import termchat.command.handlers.chatroom.*;
import termchat.command.handlers.message.DownloadCommand;
import termchat.command.handlers.message.MessageCommand;
import termchat.command.handlers.message.UploadCommand;

import java.util.*;

public class CommandRegistry {
    private final Map<String, CommandHandler> commands = new HashMap<>();
    private final HistoryCommands historyCommands = new HistoryCommands();

    public CommandRegistry() {
        registerCommands();
    }

    private void registerCommands() {
        commands.put("/help", (ignored, ctx) -> new HelpCommand().handle(null, ctx));
        commands.put("/quit", (ignored, ctx) -> ctx.stop());

        commands.put("/register", (args, ctx) -> new RegisterCommand().handle(args, ctx));
        commands.put("/login", (args, ctx) -> new LoginCommand().handle(args, ctx));
        commands.put("/logout", (ignored, ctx) -> new LogoutCommand().handle(null, ctx));

        commands.put("/rooms", (ignored, ctx) -> new RoomsCommand().handle(null, ctx));
        commands.put("/users", (ignored, ctx) -> new UsersCommand().handle(null, ctx));
        commands.put("/history", historyCommands::history);

        commands.put("/msg", (args, ctx) -> new MessageCommand().handle(args, ctx));

        commands.put("/createroom", (args, ctx) -> new CreateRoomCommand().handle(args, ctx));
        commands.put("/deleteroom", (ignored, ctx) -> new DeleteRoomCommand().handle(null, ctx));
        commands.put("/switchroom", (args, ctx) -> new SwitchRoomCommand().handle(args, ctx));

        commands.put("/rename", (args, ctx) -> new RenameRoomCommand().handle(args, ctx));
        commands.put("/adduser", (args, ctx) -> new AddUserRoomCommand().handle(args, ctx));
        commands.put("/removeuser", (args, ctx) -> new RemoveUserRoomCommand().handle(args, ctx));
        commands.put("/leave", (ignored, ctx) -> new LeaveRoomCommand().handle(null, ctx));
        commands.put("/changeowner", (args, ctx) -> new ChangeRoomOwnerCommand().handle(args, ctx));

        commands.put("/upload", (args, ctx) -> new UploadCommand().handle(args, ctx));
        commands.put("/download", (args, ctx) -> new DownloadCommand().handle(args, ctx));
    }

    public void execute(String input, CommandContext ctx) {
        if (input == null || input.isBlank()) return;

        String[] args = input.trim().split(" ");
        String command = args[0].toLowerCase();

        commands.getOrDefault(command, (_, c) -> {
            if (c.getUser() == null) {
                c.sendError("Unknown command. Type /help");
            } else {
                c.server().routeMessage(input.trim(), c.clientHandler());
            }
        }).handle(args, ctx);
    }
}