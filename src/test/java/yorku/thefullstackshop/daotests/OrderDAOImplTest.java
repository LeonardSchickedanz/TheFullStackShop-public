package yorku.thefullstackshop.daotests;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yorku.thefullstackshop.daos.implementations.OrderDAOImpl;
import yorku.thefullstackshop.models.Address;
import yorku.thefullstackshop.models.Order;
import yorku.thefullstackshop.models.User;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OrderDAOImplTest {

    private OrderDAOImpl orderDAO;
    private DataSource dataSource;
    private final int USER_ID_1 = 10;
    private final int USER_ID_2 = 20;
    private final int PRODUCT_ID = 5;
    private final int BASE_ADDRESS_ID = 1;

    @BeforeEach
    void setUp() throws SQLException {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:orderdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        dataSource = h2DataSource;

        orderDAO = new OrderDAOImpl(dataSource);
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

            stmt.execute("CREATE TABLE `User` (user_id INT PRIMARY KEY, email VARCHAR(255), password VARCHAR(255), first_name VARCHAR(255), last_name VARCHAR(255))");
            stmt.execute("CREATE TABLE Product (product_id INT PRIMARY KEY, name VARCHAR(100), price DECIMAL(10,2))");

            stmt.execute("CREATE TABLE Address (address_id INT PRIMARY KEY, street VARCHAR(255), street_number VARCHAR(10), postal_code VARCHAR(10), city VARCHAR(100), province VARCHAR(100), country VARCHAR(100))");

            stmt.execute("CREATE TABLE `Order` (order_id INT AUTO_INCREMENT PRIMARY KEY, transaction_time TIMESTAMP, total_price DECIMAL(10,2), user_id INT, address_id INT, FOREIGN KEY (user_id) REFERENCES `User`(user_id), FOREIGN KEY (address_id) REFERENCES Address(address_id))");

            stmt.execute("CREATE TABLE OrderLine (order_line_id INT AUTO_INCREMENT PRIMARY KEY, order_id INT, product_id INT, FOREIGN KEY (order_id) REFERENCES `Order`(order_id), FOREIGN KEY (product_id) REFERENCES Product(product_id))");

            stmt.execute("INSERT INTO `User` (user_id, email, password, first_name, last_name) VALUES (" + USER_ID_1 + ", 'alice@test.com', 'hash1', 'Alice', 'Smith')");
            stmt.execute("INSERT INTO `User` (user_id, email, password, first_name, last_name) VALUES (" + USER_ID_2 + ", 'bob@example.com', 'hash2', 'Bob', 'Johnson')");

            stmt.execute("INSERT INTO Product (product_id, name, price) VALUES (" + PRODUCT_ID + ", 'Magic Book', 10.00)");

            stmt.execute("INSERT INTO Address (address_id, street, street_number, postal_code, city, province, country) VALUES (" + BASE_ADDRESS_ID + ", 'Teststr.', '1', '12345', 'Teststadt', 'TS', 'Germany')");
        }
    }

    private Order createNewOrder(int userId, String timeStamp) {
        Order order = new Order();
        User user = new User();
        user.setUserId(userId);

        Address address = new Address();
        address.setAddressId(BASE_ADDRESS_ID);

        order.setUser(user);
        order.setAddress(address);
        order.setTotalPrice(new BigDecimal("150.00"));
        order.setTransactionTime(LocalDateTime.parse(timeStamp));

        return order;
    }


    @Test
    void testSave_ShouldReturnOrderWithGeneratedId() {
        Order newOrder = createNewOrder(USER_ID_1, "2025-10-25T10:00:00");
        assertNull(newOrder.getOrderId());

        Order savedOrder = orderDAO.save(newOrder);

        assertNotNull(savedOrder.getOrderId());
        assertTrue(savedOrder.getOrderId() > 0);
        assertEquals(new BigDecimal("150.00"), savedOrder.getTotalPrice());
    }

    @Test
    void testFindById_Success() {
        Order savedOrder = orderDAO.save(createNewOrder(USER_ID_1, "2025-10-25T10:00:00"));
        int id = savedOrder.getOrderId();

        Optional<Order> foundOrder = orderDAO.findById(id);

        assertTrue(foundOrder.isPresent());
        assertEquals(USER_ID_1, foundOrder.get().getUser().getUserId());
        assertEquals(BASE_ADDRESS_ID, foundOrder.get().getAddress().getAddressId());
    }

    @Test
    void testUpdate() {
        Order order = orderDAO.save(createNewOrder(USER_ID_1, "2025-10-25T10:00:00"));
        order.setTotalPrice(new BigDecimal("999.99"));

        orderDAO.update(order);

        Optional<Order> updated = orderDAO.findById(order.getOrderId());
        assertTrue(updated.isPresent());
        assertEquals(new BigDecimal("999.99"), updated.get().getTotalPrice());
    }

    @Test
    void testDelete() {
        Order savedOrder = orderDAO.save(createNewOrder(USER_ID_1, "2025-10-25T10:00:00"));
        int id = savedOrder.getOrderId();

        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM OrderLine WHERE order_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
        }

        orderDAO.delete(id);

        Optional<Order> found = orderDAO.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAllByUserId_SortingAndFiltering() {
        orderDAO.save(createNewOrder(USER_ID_1, "2025-10-25T12:00:00"));
        orderDAO.save(createNewOrder(USER_ID_2, "2025-10-25T11:00:00"));
        orderDAO.save(createNewOrder(USER_ID_1, "2025-10-25T09:00:00"));

        List<Order> orders = orderDAO.findAllByUserId(USER_ID_1);

        assertEquals(2, orders.size());

        assertTrue(orders.get(0).getTransactionTime().isAfter(orders.get(1).getTransactionTime()));
        assertEquals(12, orders.get(0).getTransactionTime().getHour());
        assertEquals(9, orders.get(1).getTransactionTime().getHour());
    }


    @Test
    void testFindSalesHistory_EmailProductAndId() throws SQLException {
        Order order1 = orderDAO.save(createNewOrder(USER_ID_1, "2025-10-25T10:00:00"));
        orderDAO.save(createNewOrder(USER_ID_2, "2025-10-25T11:00:00"));

        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO OrderLine (order_id, product_id) VALUES (?, ?)")) {
            ps.setInt(1, order1.getOrderId());
            ps.setInt(2, PRODUCT_ID);
            ps.executeUpdate();
        }

        List<Order> filteredOrders = orderDAO.findSalesHistory("alice", "Book", null, null);

        assertEquals(1, filteredOrders.size());
        Order foundOrder = filteredOrders.get(0);

        assertEquals(order1.getOrderId(), foundOrder.getOrderId());
        assertEquals(USER_ID_1, foundOrder.getUser().getUserId());
        assertEquals("alice@test.com", foundOrder.getUser().getEmail());

        List<Order> allUserOrders = orderDAO.findSalesHistory("test.com", null, null, null);
        assertTrue(allUserOrders.stream().anyMatch(o -> o.getUser().getUserId().equals(USER_ID_1)));
    }

    @Test
    void testFindSalesHistory_DateRange() {
        orderDAO.save(createNewOrder(USER_ID_1, "2025-10-25T10:00:00"));
        orderDAO.save(createNewOrder(USER_ID_1, "2025-10-26T12:00:00"));
        orderDAO.save(createNewOrder(USER_ID_1, "2025-10-30T10:00:00"));

        List<Order> filteredOrders = orderDAO.findSalesHistory(null, null, "2025-10-24", "2025-10-27");

        assertEquals(2, filteredOrders.size());
        assertEquals(12, filteredOrders.get(0).getTransactionTime().getHour());
        assertEquals(10, filteredOrders.get(1).getTransactionTime().getHour());
    }

    @Test
    void testFindSalesHistory_NoMatch() {
        orderDAO.save(createNewOrder(USER_ID_1, "2025-10-25T10:00:00"));
        List<Order> filteredOrders = orderDAO.findSalesHistory(null, "Unicorn Horns", null, null);
        assertTrue(filteredOrders.isEmpty());
    }
}