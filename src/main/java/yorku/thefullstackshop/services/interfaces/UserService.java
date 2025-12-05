package yorku.thefullstackshop.services.interfaces;

import yorku.thefullstackshop.models.User;

import java.util.Optional;

public interface UserService {
    User registerUser(User user);

    Optional<User> login(String email, String password);

    Optional<User> findUserById(int id);

    boolean isEmailTaken(String email);
}