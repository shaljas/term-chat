package termchat.service;

import termchat.exceptions.UsernameTakenException;
import termchat.model.User;
import termchat.repository.UserRepository;

import java.util.UUID;

import static termchat.service.EncryptionService.encryptPassword;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public void registerUser(String username, String password, String email) {
        if (userRepository.usernameExists(username)) {
            throw new UsernameTakenException(username);
        }
        User newUser = new User(UUID.randomUUID().toString(), username, encryptPassword(password), email);
        userRepository.saveUser(newUser);
    }

    public User loginUser(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(u -> u.getPasswordHash().equals(encryptPassword(password)))
                .orElse(null);
    }
}
