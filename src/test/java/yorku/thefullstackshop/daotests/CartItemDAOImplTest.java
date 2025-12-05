package yorku.thefullstackshop.daotests;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yorku.thefullstackshop.daos.implementations.CartItemDAOImpl;
import yorku.thefullstackshop.models.CartItem;
import yorku.thefullstackshop.models.Product;
import yorku.thefullstackshop.models.ShoppingCart;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CartItemDAOImplTest {

    private CartItemDAOImpl cartItemDAO;
    private DataSource dataSource;
    private final int TEST_PRODUCT_ID = 10;
    private final int TEST_CART_ID = 20;
    private final int TEST_BRAND_ID = 30;
    private final int TEST_CATEGORY_ID = 40;

    @BeforeEach
    void setUp() throws SQLException {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:cartitemdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        dataSource = h2DataSource;

        cartItemDAO = new CartItemDAOImpl(dataSource);

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

            stmt.execute("CREATE TABLE Brand (brand_id INT PRIMARY KEY, name VARCHAR(100))");
            stmt.execute("CREATE TABLE Category (category_id INT PRIMARY KEY, name VARCHAR(100))");
            stmt.execute("CREATE TABLE ShoppingCart (shopping_cart_id INT PRIMARY KEY)");

            stmt.execute("CREATE TABLE Product (" + "product_id INT PRIMARY KEY, " + "name VARCHAR(150), " + "description TEXT, " + "price DECIMAL(10, 2), " + "img_url VARCHAR(255), " + "quantity INT, " + "brand_id INT, " + "category_id INT, " + "FOREIGN KEY (brand_id) REFERENCES Brand(brand_id), " + "FOREIGN KEY (category_id) REFERENCES Category(category_id)" + ")");

            stmt.execute("CREATE TABLE CartItem (" + "cart_item_id INT AUTO_INCREMENT PRIMARY KEY, " + "quantity INT NOT NULL, " + "shopping_cart_id INT NOT NULL, " + "product_id INT NOT NULL, " + "FOREIGN KEY (shopping_cart_id) REFERENCES ShoppingCart(shopping_cart_id), " + "FOREIGN KEY (product_id) REFERENCES Product(product_id)" + ")");

            stmt.execute("INSERT INTO Brand (brand_id, name) VALUES (" + TEST_BRAND_ID + ", 'TestBrandName')");
            stmt.execute("INSERT INTO Category (category_id, name) VALUES (" + TEST_CATEGORY_ID + ", 'TestCategory')");
            stmt.execute("INSERT INTO ShoppingCart (shopping_cart_id) VALUES (" + TEST_CART_ID + ")");
            stmt.execute("INSERT INTO Product (product_id, name, description, price, img_url, quantity, brand_id, category_id) " + "VALUES (" + TEST_PRODUCT_ID + ", 'Laptop Pro', 'High end laptop', 1500.00, 'url_img', 50, " + TEST_BRAND_ID + ", " + TEST_CATEGORY_ID + ")");
        }
    }

    private CartItem createNewCartItem(int productId, int quantity, int cartId) {
        CartItem item = new CartItem();
        item.setQuantity(quantity);

        ShoppingCart cart = new ShoppingCart();
        cart.setShoppingCartId(cartId);
        item.setShoppingCart(cart);

        Product product = new Product();
        product.setProductId(productId);
        item.setProduct(product);

        return item;
    }


    @Test
    void testSave_ShouldReturnItemWithGeneratedId() {
        CartItem item = createNewCartItem(TEST_PRODUCT_ID, 1, TEST_CART_ID);
        assertNull(item.getCartItemId());

        CartItem savedItem = cartItemDAO.save(item);

        assertNotNull(savedItem.getCartItemId());
        assertEquals(1, savedItem.getQuantity());
    }

    @Test
    void testFindById_Success() {
        CartItem savedItem = cartItemDAO.save(createNewCartItem(TEST_PRODUCT_ID, 5, TEST_CART_ID));
        int id = savedItem.getCartItemId();

        Optional<CartItem> found = cartItemDAO.findById(id);

        assertTrue(found.isPresent());
        assertEquals(5, found.get().getQuantity());
        assertEquals(TEST_PRODUCT_ID, found.get().getProduct().getProductId());
    }

    @Test
    void testUpdate() {
        CartItem item = cartItemDAO.save(createNewCartItem(TEST_PRODUCT_ID, 2, TEST_CART_ID));
        item.setQuantity(7);

        cartItemDAO.update(item);

        Optional<CartItem> updated = cartItemDAO.findById(item.getCartItemId());
        assertTrue(updated.isPresent());
        assertEquals(7, updated.get().getQuantity());
    }

    @Test
    void testDelete() {
        CartItem item = cartItemDAO.save(createNewCartItem(TEST_PRODUCT_ID, 1, TEST_CART_ID));
        int id = item.getCartItemId();

        cartItemDAO.delete(id);

        Optional<CartItem> found = cartItemDAO.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        cartItemDAO.save(createNewCartItem(TEST_PRODUCT_ID, 1, TEST_CART_ID));
        cartItemDAO.save(createNewCartItem(TEST_PRODUCT_ID, 3, TEST_CART_ID));

        List<CartItem> items = cartItemDAO.findAll();

        assertEquals(2, items.size());
        assertEquals(4, items.stream().mapToInt(CartItem::getQuantity).sum());
    }

    @Test
    void testFindAllByShoppingCartId_SuccessWithDetails() {
        cartItemDAO.save(createNewCartItem(TEST_PRODUCT_ID, 1, TEST_CART_ID));

        int OTHER_CART_ID = 99;
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO ShoppingCart (shopping_cart_id) VALUES (" + OTHER_CART_ID + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }
        cartItemDAO.save(createNewCartItem(TEST_PRODUCT_ID, 5, OTHER_CART_ID));


        List<CartItem> items = cartItemDAO.findAllByShoppingCartId(TEST_CART_ID);

        assertEquals(1, items.size());

        CartItem item = items.get(0);
        assertNotNull(item.getProduct());
        assertEquals(TEST_PRODUCT_ID, item.getProduct().getProductId());
        assertEquals("Laptop Pro", item.getProduct().getName());
        assertNotNull(item.getProduct().getBrand());
        assertEquals("TestBrandName", item.getProduct().getBrand().getName());
        assertEquals(50, item.getProduct().getQuantity());
    }

    @Test
    void testFindAllByShoppingCartId_NotFound() {
        List<CartItem> items = cartItemDAO.findAllByShoppingCartId(999);
        assertTrue(items.isEmpty());
    }
}