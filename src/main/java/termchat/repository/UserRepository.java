package termchat.repository;

import termchat.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {
    private final List<User> users = new ArrayList<>();

    public void saveUser(User user) {
        users.add(user);
    }

    // Optional class - solution for representing optional values instead of null references. <https://www.baeldung.com/java-optional>
    public Optional<User> findByUsername(String username) {
        return users.stream().filter(u -> u.getUsername().equals(username)).findFirst();
    }

    public boolean usernameExists(String username) {
        return findByUsername(username).isPresent();
    }
}
