package termchat.exceptions;

public class UsernameTakenException extends RuntimeException {
    public UsernameTakenException(String username) {
        super("Error: username already taken");
    }
}
