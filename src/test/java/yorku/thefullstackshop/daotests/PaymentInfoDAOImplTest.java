package yorku.thefullstackshop.daotests;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yorku.thefullstackshop.daos.implementations.PaymentInfoDAOImpl;
import yorku.thefullstackshop.models.PaymentInfo;
import yorku.thefullstackshop.models.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PaymentInfoDAOImplTest {

    private PaymentInfoDAOImpl paymentInfoDAO;
    private DataSource dataSource;
    private final int TEST_USER_ID = 10;


    @BeforeEach
    void setUp() throws SQLException {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:paymentinfodb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        dataSource = h2DataSource;

        paymentInfoDAO = new PaymentInfoDAOImpl(dataSource);
        createSchemaAndInsertBaseData();
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
        }
    }


    private void createSchemaAndInsertBaseData() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE \"User\" (user_id INT PRIMARY KEY, first_name VARCHAR(100))");
            stmt.execute("INSERT INTO \"User\" (user_id, first_name) VALUES (" + TEST_USER_ID + ", 'PaymentUser')");

            stmt.execute("CREATE TABLE PaymentInfo (" +
                    "payment_info_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "card_number VARCHAR(255) NOT NULL, " +
                    "expiry_date VARCHAR(10) NOT NULL, " +
                    "cvc VARCHAR(10), " +
                    "user_id INT NOT NULL, " +
                    "last_used TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (user_id) REFERENCES \"User\"(user_id) ON DELETE CASCADE" +
                    ")");
        }
    }

    private PaymentInfo createNewPaymentInfo() {
        PaymentInfo info = new PaymentInfo();
        info.setCardNumber("1111222233334444");
        info.setExpiryDate("12/28");
        info.setCvc("123");
        User user = new User();
        user.setUserId(TEST_USER_ID);
        info.setUser(user);
        return info;
    }


    @Test
    void testSave_ShouldReturnInfoWithGeneratedId() {
        PaymentInfo newInfo = createNewPaymentInfo();
        assertNull(newInfo.getPaymentInfoId());

        PaymentInfo savedInfo = paymentInfoDAO.save(newInfo);

        assertNotNull(savedInfo.getPaymentInfoId());
        assertEquals("12/28", savedInfo.getExpiryDate());
    }

    @Test
    void testFindById_Success() {
        PaymentInfo savedInfo = paymentInfoDAO.save(createNewPaymentInfo());
        int id = savedInfo.getPaymentInfoId();

        Optional<PaymentInfo> found = paymentInfoDAO.findById(id);

        assertTrue(found.isPresent());
        assertEquals("1111222233334444", found.get().getCardNumber());
    }

    @Test
    void testUpdate() {
        PaymentInfo info = paymentInfoDAO.save(createNewPaymentInfo());
        info.setCardNumber("9999888877776666");

        paymentInfoDAO.update(info);

        Optional<PaymentInfo> updated = paymentInfoDAO.findById(info.getPaymentInfoId());
        assertTrue(updated.isPresent());
        assertEquals("9999888877776666", updated.get().getCardNumber());
    }

    @Test
    void testDelete() {
        PaymentInfo savedInfo = paymentInfoDAO.save(createNewPaymentInfo());
        int id = savedInfo.getPaymentInfoId();

        paymentInfoDAO.delete(id);

        Optional<PaymentInfo> found = paymentInfoDAO.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        paymentInfoDAO.save(createNewPaymentInfo());
        paymentInfoDAO.save(createNewPaymentInfo());

        List<PaymentInfo> list = paymentInfoDAO.findAll();

        assertEquals(2, list.size());
    }

    @Test
    void testFindLatestPaymentInfoByUserId() throws InterruptedException {
        PaymentInfo oldInfo = createNewPaymentInfo();
        oldInfo.setCardNumber("OLD_CARD");
        paymentInfoDAO.save(oldInfo);

        Thread.sleep(100);

        PaymentInfo newInfo = createNewPaymentInfo();
        newInfo.setCardNumber("NEW_CARD");
        paymentInfoDAO.save(newInfo);

        Optional<PaymentInfo> result = paymentInfoDAO.findLatestPaymentInfoByUserId(TEST_USER_ID);

        assertTrue(result.isPresent());
        assertEquals("NEW_CARD", result.get().getCardNumber());
    }
}