package yorku.thefullstackshop.daotests;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yorku.thefullstackshop.daos.implementations.UserDAOImpl;
import yorku.thefullstackshop.models.Role;
import yorku.thefullstackshop.models.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOImplTest {

    private UserDAOImpl userDAO;
    private DataSource dataSource;
    private final int TEST_ROLE_ID = 1;

    @BeforeEach
    void setUp() throws SQLException {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:userdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        dataSource = h2DataSource;

        userDAO = new UserDAOImpl(dataSource);
        createSchemaAndInsertBaseData();
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
        }
    }

    private void createSchemaAndInsertBaseData() throws SQLException {
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE Role (role_id INT PRIMARY KEY, name VARCHAR(50))");
            stmt.execute("INSERT INTO Role (role_id, name) VALUES (" + TEST_ROLE_ID + ", 'CUSTOMER')");

            stmt.execute("CREATE TABLE `User` (" + "user_id INT AUTO_INCREMENT PRIMARY KEY, " + "first_name VARCHAR(100) NOT NULL, " + "last_name VARCHAR(100) NOT NULL, " + "email VARCHAR(150) NOT NULL UNIQUE, " + "password VARCHAR(255) NOT NULL, " + "role_id INT NOT NULL, " + "FOREIGN KEY (role_id) REFERENCES Role(role_id)" + ")");
        }
    }

    private User createNewUser(String emailSuffix) {
        User user = new User();
        user.setFirstName("Alice");
        user.setLastName("Tester");
        user.setEmail("alice" + emailSuffix + "@test.com");
        user.setPassword("hashed_password");
        Role role = new Role();
        role.setRoleId(TEST_ROLE_ID);
        user.setRole(role);
        return user;
    }


    @Test
    void testSave_ShouldReturnUserWithGeneratedId() {
        User newUser = createNewUser("1");
        assertNull(newUser.getUserId());

        User savedUser = userDAO.save(newUser);

        assertNotNull(savedUser.getUserId());
        assertEquals("Alice", savedUser.getFirstName());
    }

    @Test
    void testFindById_Success() {
        User savedUser = userDAO.save(createNewUser("2"));
        int id = savedUser.getUserId();

        Optional<User> found = userDAO.findById(id);

        assertTrue(found.isPresent());
        assertEquals("Tester", found.get().getLastName());
        assertEquals(TEST_ROLE_ID, found.get().getRole().getRoleId());
    }

    @Test
    void testFindById_NotFound() {
        Optional<User> found = userDAO.findById(999);
        assertFalse(found.isPresent());
    }

    @Test
    void testUpdate() {
        User user = userDAO.save(createNewUser("3"));
        user.setEmail("new@email.com");
        user.setFirstName("Bob");

        userDAO.update(user);

        Optional<User> updated = userDAO.findById(user.getUserId());
        assertTrue(updated.isPresent());
        assertEquals("Bob", updated.get().getFirstName());
        assertEquals("new@email.com", updated.get().getEmail());
    }

    @Test
    void testDelete() {
        User savedUser = userDAO.save(createNewUser("4"));
        int id = savedUser.getUserId();

        userDAO.delete(id);

        Optional<User> found = userDAO.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        userDAO.save(createNewUser("5"));
        userDAO.save(createNewUser("6"));

        List<User> users = userDAO.findAll();

        assertEquals(2, users.size());
    }

    @Test
    void testFindByEmail_Success() {
        userDAO.save(createNewUser("7"));
        String targetEmail = "alice7@test.com";

        Optional<User> found = userDAO.findByEmail(targetEmail);

        assertTrue(found.isPresent());
        assertEquals(targetEmail, found.get().getEmail());
    }

    @Test
    void testFindByEmail_NotFound() {
        Optional<User> found = userDAO.findByEmail("nonexistent@test.com");
        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByEmail_True() {
        userDAO.save(createNewUser("8"));
        assertTrue(userDAO.existsByEmail("alice8@test.com"));
    }

    @Test
    void testExistsByEmail_False() {
        assertFalse(userDAO.existsByEmail("alice9@test.com"));
    }
}