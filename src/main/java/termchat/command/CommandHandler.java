package termchat.command;

@FunctionalInterface
public interface CommandHandler {
    void handle(String[] args, CommandContext ctx);
}
