package termchat.repository;

import com.google.gson.reflect.TypeToken;
import termchat.model.User;
import termchat.persistence.JsonStorageService;
import termchat.persistence.StoredUser;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {
    private static final String USERS_FILE = "users.json";

    private final List<User> users = new ArrayList<>();
    private final JsonStorageService storageService = new JsonStorageService();

    public UserRepository() {
        loadUsersFromStorage();
    }


    private void loadUsersFromStorage() {
        Type userListType = new TypeToken<List<StoredUser>>() {}.getType();
        List<StoredUser> storedUsers = storageService.load(USERS_FILE, userListType);

        if (storedUsers == null) return;
        for (StoredUser storedUser : storedUsers) {
            User user = new User(storedUser.getUserId(), storedUser.getUsername(), storedUser.getPasswordHash());
            users.add(user);
        }
    }

    public void saveUser(User user) {
        users.add(user);
        saveUsersToStorage();
    }

    private void saveUsersToStorage() {
        List<StoredUser> storedUsers = users.stream().map(user -> new StoredUser(
                user.getUserId(),
                user.getUsername(),
                user.getPasswordHash()
        )).toList();
        storageService.save(USERS_FILE, storedUsers);
    }

    // Optional class - solution for representing optional values instead of null references. <https://www.baeldung.com/java-optional>
    public Optional<User> findByUsername(String username) {
        return users.stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst();
    }

    public boolean usernameExists(String username) {
        return findByUsername(username).isPresent();
    }
}
