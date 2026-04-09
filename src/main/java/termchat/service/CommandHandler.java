package termchat.service;

@FunctionalInterface
public interface CommandHandler {
    void handle(String[] args);
}
