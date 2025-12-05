package yorku.thefullstackshop.servicetests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import yorku.thefullstackshop.daos.interfaces.UserDAO;
import yorku.thefullstackshop.models.Role;
import yorku.thefullstackshop.models.User;
import yorku.thefullstackshop.services.implementations.UserServiceImpl;
import yorku.thefullstackshop.utils.PasswordHashingUtil;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private UserServiceImpl userService;

    private final String RAW_PASSWORD = "password123";
    private final String HASHED_PASSWORD = "hashed_password_abc";
    private final String TEST_EMAIL = "test@example.com";


    @Test
    void testRegisterUser_Success() {
        User user = new User();
        user.setEmail(TEST_EMAIL);
        user.setPassword(RAW_PASSWORD);

        when(userDAO.existsByEmail(TEST_EMAIL)).thenReturn(false);

        try (MockedStatic<PasswordHashingUtil> util = mockStatic(PasswordHashingUtil.class)) {
            util.when(() -> PasswordHashingUtil.hashPassword(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
            when(userDAO.save(any(User.class))).thenReturn(user);

            User registeredUser = userService.registerUser(user);

            assertEquals(HASHED_PASSWORD, registeredUser.getPassword());
            assertEquals(Role.CUSTOMER_ID, registeredUser.getRole().getRoleId());
            verify(userDAO).existsByEmail(TEST_EMAIL);
            verify(userDAO).save(user);
        }
    }


    @Test
    void testLogin_Success() {
        User user = new User();
        user.setPassword(HASHED_PASSWORD);

        when(userDAO.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));

        try (MockedStatic<PasswordHashingUtil> util = mockStatic(PasswordHashingUtil.class)) {
            util.when(() -> PasswordHashingUtil.checkPassword(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);

            Optional<User> result = userService.login(TEST_EMAIL, RAW_PASSWORD);

            assertTrue(result.isPresent());
            verify(userDAO).findByEmail(TEST_EMAIL);
            util.verify(() -> PasswordHashingUtil.checkPassword(RAW_PASSWORD, HASHED_PASSWORD), times(1));
        }
    }

    @Test
    void testLogin_Failure_IncorrectPassword() {
        User user = new User();
        user.setPassword(HASHED_PASSWORD);

        when(userDAO.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));

        try (MockedStatic<PasswordHashingUtil> util = mockStatic(PasswordHashingUtil.class)) {
            util.when(() -> PasswordHashingUtil.checkPassword(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(false);

            Optional<User> result = userService.login(TEST_EMAIL, RAW_PASSWORD);

            assertFalse(result.isPresent());
            util.verify(() -> PasswordHashingUtil.checkPassword(RAW_PASSWORD, HASHED_PASSWORD), times(1));
        }
    }

    @Test
    void testLogin_Failure_UserNotFound() {
        when(userDAO.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        Optional<User> result = userService.login(TEST_EMAIL, RAW_PASSWORD);

        assertFalse(result.isPresent());
        verify(userDAO).findByEmail(TEST_EMAIL);
    }
}