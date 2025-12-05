package yorku.thefullstackshop.daotests;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yorku.thefullstackshop.daos.implementations.ShoppingCartDAOImpl;
import yorku.thefullstackshop.models.ShoppingCart;
import yorku.thefullstackshop.models.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingCartDAOImplTest {

    private ShoppingCartDAOImpl shoppingCartDAO;
    private DataSource dataSource;
    private final int TEST_USER_ID = 5;
    private final String TEST_SESSION_ID = "SESSION_XYZ";

    @BeforeEach
    void setUp() throws SQLException {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:cartdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        dataSource = h2DataSource;

        shoppingCartDAO = new ShoppingCartDAOImpl(dataSource);
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

            stmt.execute("CREATE TABLE \"User\" (user_id INT PRIMARY KEY, first_name VARCHAR(100))");
            stmt.execute("INSERT INTO \"User\" (user_id, first_name) VALUES (" + TEST_USER_ID + ", 'CartUser')");

            stmt.execute("CREATE TABLE ShoppingCart (" + "shopping_cart_id INT AUTO_INCREMENT PRIMARY KEY, " + "user_id INT NULL, " + "session_id VARCHAR(100) NULL, " + "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " + "FOREIGN KEY (user_id) REFERENCES \"User\"(user_id)" + ")");
        }
    }

    private ShoppingCart createNewCart(boolean isUser) {
        ShoppingCart cart = new ShoppingCart();
        if (isUser) {
            User user = new User();
            user.setUserId(TEST_USER_ID);
            cart.setUser(user);
            cart.setSessionId(null);
        } else {
            cart.setSessionId(TEST_SESSION_ID);
            cart.setUser(null);
        }
        return cart;
    }

    @Test
    void testSave_GuestCart() {
        ShoppingCart newCart = createNewCart(false);

        ShoppingCart savedCart = shoppingCartDAO.save(newCart);

        assertNotNull(savedCart.getShoppingCartId());
        assertEquals(TEST_SESSION_ID, savedCart.getSessionId());
        assertNull(savedCart.getUser());
    }

    @Test
    void testSave_UserCart() {
        ShoppingCart newCart = createNewCart(true);

        ShoppingCart savedCart = shoppingCartDAO.save(newCart);

        assertNotNull(savedCart.getShoppingCartId());
        assertNotNull(savedCart.getUser());
        assertEquals(TEST_USER_ID, savedCart.getUser().getUserId());
        assertNull(savedCart.getSessionId());
    }

    @Test
    void testFindById_UserCartSuccess() {
        ShoppingCart savedCart = shoppingCartDAO.save(createNewCart(true));
        int id = savedCart.getShoppingCartId();

        Optional<ShoppingCart> found = shoppingCartDAO.findById(id);

        assertTrue(found.isPresent());
        assertEquals(TEST_USER_ID, found.get().getUser().getUserId());
    }

    @Test
    void testFindById_GuestCartSuccess() {
        ShoppingCart savedCart = shoppingCartDAO.save(createNewCart(false));
        int id = savedCart.getShoppingCartId();

        Optional<ShoppingCart> found = shoppingCartDAO.findById(id);

        assertTrue(found.isPresent());
        assertEquals(TEST_SESSION_ID, found.get().getSessionId());
        assertNull(found.get().getUser());
    }

    @Test
    void testUpdate_ConvertGuestToUser() {
        ShoppingCart guestCart = shoppingCartDAO.save(createNewCart(false));
        int id = guestCart.getShoppingCartId();

        User user = new User();
        user.setUserId(TEST_USER_ID);
        guestCart.setUser(user);
        guestCart.setSessionId(null);

        shoppingCartDAO.update(guestCart);

        Optional<ShoppingCart> updated = shoppingCartDAO.findById(id);
        assertTrue(updated.isPresent());
        assertNotNull(updated.get().getUser());
        assertEquals(TEST_USER_ID, updated.get().getUser().getUserId());
        assertNull(updated.get().getSessionId());
    }

    @Test
    void testDelete() {
        ShoppingCart savedCart = shoppingCartDAO.save(createNewCart(false));
        int id = savedCart.getShoppingCartId();

        shoppingCartDAO.delete(id);

        Optional<ShoppingCart> found = shoppingCartDAO.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        shoppingCartDAO.save(createNewCart(false));
        shoppingCartDAO.save(createNewCart(true));

        List<ShoppingCart> carts = shoppingCartDAO.findAll();

        assertEquals(2, carts.size());
    }
}