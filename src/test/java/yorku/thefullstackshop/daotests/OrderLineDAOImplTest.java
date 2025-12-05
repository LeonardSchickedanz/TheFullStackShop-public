package yorku.thefullstackshop.daotests;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yorku.thefullstackshop.daos.implementations.OrderLineDAOImpl;
import yorku.thefullstackshop.models.Order;
import yorku.thefullstackshop.models.OrderLine;
import yorku.thefullstackshop.models.Product;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OrderLineDAOImplTest {

    private OrderLineDAOImpl orderLineDAO;
    private DataSource dataSource;
    private final int TEST_ORDER_ID = 100;
    private final int TEST_PRODUCT_ID = 200;
    private final int TEST_BRAND_ID = 300;

    @BeforeEach
    void setUp() throws SQLException {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:orderlinedb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        dataSource = h2DataSource;

        orderLineDAO = new OrderLineDAOImpl(dataSource);
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

            stmt.execute("CREATE TABLE \"User\" (user_id INT PRIMARY KEY, email VARCHAR(255))");
            stmt.execute("INSERT INTO \"User\" (user_id, email) VALUES (1, 'dummy@test.com')");

            stmt.execute("CREATE TABLE Brand (brand_id INT PRIMARY KEY, name VARCHAR(100))");
            stmt.execute("INSERT INTO Brand (brand_id, name) VALUES (" + TEST_BRAND_ID + ", 'TestBrandName')");

            stmt.execute("CREATE TABLE Product (product_id INT PRIMARY KEY, name VARCHAR(150), img_url VARCHAR(255), brand_id INT, FOREIGN KEY (brand_id) REFERENCES Brand(brand_id))");
            stmt.execute("INSERT INTO Product (product_id, name, img_url, brand_id) VALUES (" + TEST_PRODUCT_ID + ", 'Widget X', 'url_img', " + TEST_BRAND_ID + ")");

            stmt.execute("CREATE TABLE `Order` (order_id INT PRIMARY KEY, transaction_time TIMESTAMP, total_price DECIMAL(10,2), user_id INT, FOREIGN KEY (user_id) REFERENCES \"User\"(user_id))");

            stmt.execute("INSERT INTO `Order` (order_id, transaction_time, total_price, user_id) VALUES (" + TEST_ORDER_ID + ", NOW(), 50.00, 1)");

            stmt.execute("CREATE TABLE OrderLine (" + "order_line_id INT AUTO_INCREMENT PRIMARY KEY, " + "quantity INT NOT NULL, " + "unit_price DECIMAL(10, 2) NOT NULL, " + "order_id INT NOT NULL, " + "product_id INT NOT NULL, " + "FOREIGN KEY (order_id) REFERENCES `Order`(order_id), " + "FOREIGN KEY (product_id) REFERENCES Product(product_id)" + ")");
        }
    }

    private OrderLine createNewOrderLine(int productId, int orderId) {
        OrderLine line = new OrderLine();
        line.setQuantity(2);
        line.setUnitPrice(new BigDecimal("10.50"));

        Order order = new Order();
        order.setOrderId(orderId);
        line.setOrder(order);

        Product product = new Product();
        product.setProductId(productId);
        line.setProduct(product);

        return line;
    }


    @Test
    void testSave_ShouldReturnLineWithGeneratedId() {
        OrderLine newLine = createNewOrderLine(TEST_PRODUCT_ID, TEST_ORDER_ID);
        assertNull(newLine.getOrderLineId());

        OrderLine savedLine = orderLineDAO.save(newLine);

        assertNotNull(savedLine.getOrderLineId());
        assertEquals(2, savedLine.getQuantity());
    }

    @Test
    void testFindById_Success() {
        OrderLine savedLine = orderLineDAO.save(createNewOrderLine(TEST_PRODUCT_ID, TEST_ORDER_ID));
        int id = savedLine.getOrderLineId();

        Optional<OrderLine> found = orderLineDAO.findById(id);

        assertTrue(found.isPresent());
        assertEquals(TEST_PRODUCT_ID, found.get().getProduct().getProductId());
    }

    @Test
    void testUpdate() {
        OrderLine line = orderLineDAO.save(createNewOrderLine(TEST_PRODUCT_ID, TEST_ORDER_ID));
        line.setQuantity(99);

        orderLineDAO.update(line);

        Optional<OrderLine> updated = orderLineDAO.findById(line.getOrderLineId());
        assertTrue(updated.isPresent());
        assertEquals(99, updated.get().getQuantity());
    }

    @Test
    void testDelete() {
        OrderLine line = orderLineDAO.save(createNewOrderLine(TEST_PRODUCT_ID, TEST_ORDER_ID));
        int id = line.getOrderLineId();

        orderLineDAO.delete(id);

        Optional<OrderLine> found = orderLineDAO.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAllByOrderId_NotFound() {
        List<OrderLine> lines = orderLineDAO.findAllByOrderId(999);
        assertTrue(lines.isEmpty());
    }
}