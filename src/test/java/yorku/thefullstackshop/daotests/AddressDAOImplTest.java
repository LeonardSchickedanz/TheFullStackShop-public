package yorku.thefullstackshop.daotests;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yorku.thefullstackshop.daos.implementations.AddressDAOImpl;
import yorku.thefullstackshop.models.Address;
import yorku.thefullstackshop.models.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AddressDAOImplTest {

    private AddressDAOImpl addressDAO;
    private DataSource dataSource;
    private final int TEST_USER_ID = 1;

    @BeforeEach
    void setUp() throws SQLException {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        dataSource = h2DataSource;

        addressDAO = new AddressDAOImpl(dataSource);

        createSchema();
        insertTestUser();
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
        }
    }

    private void createSchema() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE \"User\" (user_id INT PRIMARY KEY, first_name VARCHAR(100))");

            stmt.execute("CREATE TABLE Address (" +
                    "address_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "street VARCHAR(150) NOT NULL, " +
                    "street_number VARCHAR(20) NOT NULL, " +
                    "postal_code VARCHAR(20) NOT NULL, " +
                    "city VARCHAR(100) NOT NULL, " +
                    "province VARCHAR(50) NOT NULL, " +
                    "country VARCHAR(50) NOT NULL DEFAULT 'Canada', " +
                    "user_id INT NOT NULL, " +
                    "last_used TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (user_id) REFERENCES \"User\"(user_id) ON DELETE CASCADE" +
                    ")");
        }
    }

    private void insertTestUser() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO \"User\" (user_id, first_name) VALUES (?, ?)")) {
            ps.setInt(1, TEST_USER_ID);
            ps.setString(2, "TestUser");
            ps.executeUpdate();
        }
    }

    private Address createNewAddress() {
        Address address = new Address();
        address.setStreet("Teststrasse");
        address.setStreetNumber("1A");
        address.setPostalCode("12345");
        address.setCity("Teststadt");
        address.setProvince("Testprovinz");
        address.setCountry("Testland");
        User user = new User();
        user.setUserId(TEST_USER_ID);
        address.setUser(user);
        return address;
    }


    @Test
    void testSave_ShouldReturnAddressWithGeneratedId() {
        Address newAddress = createNewAddress();
        assertNull(newAddress.getAddressId());

        Address savedAddress = addressDAO.save(newAddress);

        assertNotNull(savedAddress.getAddressId());
        assertTrue(savedAddress.getAddressId() > 0);
        assertEquals("Teststadt", savedAddress.getCity());
    }

    @Test
    void testFindById_Success() {
        Address savedAddress = addressDAO.save(createNewAddress());
        int id = savedAddress.getAddressId();

        Optional<Address> foundAddress = addressDAO.findById(id);

        assertTrue(foundAddress.isPresent());
        assertEquals("Teststrasse", foundAddress.get().getStreet());
        assertEquals(TEST_USER_ID, foundAddress.get().getUser().getUserId());
    }

    @Test
    void testFindById_NotFound() {
        Optional<Address> foundAddress = addressDAO.findById(999);
        assertFalse(foundAddress.isPresent());
    }

    @Test
    void testFindAll() {
        Address a1 = createNewAddress();
        a1.setCity("City 1");
        addressDAO.save(a1);

        Address a2 = createNewAddress();
        a2.setCity("City 2");
        addressDAO.save(a2);

        List<Address> addresses = addressDAO.findAll();

        assertNotNull(addresses);
        assertEquals(2, addresses.size());
    }

    @Test
    void testUpdate() {
        Address address = addressDAO.save(createNewAddress());
        int id = address.getAddressId();

        address.setCity("Neue Stadt");
        address.setPostalCode("99999");

        Address updatedResult = addressDAO.update(address);

        assertEquals("Neue Stadt", updatedResult.getCity());

        Optional<Address> dbAddress = addressDAO.findById(id);
        assertTrue(dbAddress.isPresent());
        assertEquals("Neue Stadt", dbAddress.get().getCity());
        assertEquals("99999", dbAddress.get().getPostalCode());
    }

    @Test
    void testDelete() {
        Address savedAddress = addressDAO.save(createNewAddress());
        int id = savedAddress.getAddressId();

        addressDAO.delete(id);

        Optional<Address> foundAddress = addressDAO.findById(id);
        assertFalse(foundAddress.isPresent());
    }

    @Test
    void testFindLatestAddressByUserId() throws InterruptedException {
        Address oldAddress = createNewAddress();
        oldAddress.setStreet("Old Street");
        addressDAO.save(oldAddress);

        Thread.sleep(100);

        Address newAddress = createNewAddress();
        newAddress.setStreet("New Street");
        addressDAO.save(newAddress);

        Optional<Address> result = addressDAO.findLatestAddressByUserId(TEST_USER_ID);

        assertTrue(result.isPresent());
        assertEquals("New Street", result.get().getStreet());
        assertNotEquals("Old Street", result.get().getStreet());
    }
}