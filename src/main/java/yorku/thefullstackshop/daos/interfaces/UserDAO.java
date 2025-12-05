package yorku.thefullstackshop.daos.interfaces;

import yorku.thefullstackshop.models.User;

import java.util.Optional;

public interface UserDAO extends BaseDAO<User> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
