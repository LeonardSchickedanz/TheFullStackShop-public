package yorku.thefullstackshop.services.implementations;

import yorku.thefullstackshop.daos.implementations.UserDAOImpl;
import yorku.thefullstackshop.daos.interfaces.UserDAO;
import yorku.thefullstackshop.models.Role;
import yorku.thefullstackshop.models.User;
import yorku.thefullstackshop.services.interfaces.UserService;
import yorku.thefullstackshop.utils.PasswordHashingUtil;

import java.util.Optional;

public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;
    private final PasswordHashingUtil passwordHashingUtil;

    public UserServiceImpl(UserDAO userDAO, PasswordHashingUtil passwordHashingUtil) {
        this.userDAO = userDAO;
        this.passwordHashingUtil = passwordHashingUtil;
    }

    public UserServiceImpl() {
        this.userDAO = new UserDAOImpl();
        this.passwordHashingUtil = new PasswordHashingUtil();
    }

    @Override
    public User registerUser(User user) {
        if (userDAO.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email is already taken.");
        }

        String hashedPassword = passwordHashingUtil.hashPassword(user.getPassword());
        user.setPassword(hashedPassword);

        Role role = new Role();
        role.setRoleId(Role.CUSTOMER_ID);
        user.setRole(role);

        return userDAO.save(user);
    }

    @Override
    public Optional<User> login(String email, String password) {
        Optional<User> userOptional = userDAO.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordHashingUtil.checkPassword(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findUserById(int id){
        return userDAO.findById(id);
    }

    @Override
    public boolean isEmailTaken(String email) {
        return userDAO.existsByEmail(email);
    }
}