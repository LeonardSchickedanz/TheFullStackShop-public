package yorku.thefullstackshop.daotests;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yorku.thefullstackshop.daos.implementations.BrandDAOImpl;
import yorku.thefullstackshop.models.Brand;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BrandDAOImplTest {

    private BrandDAOImpl brandDAO;
    private DataSource dataSource;

    @BeforeEach
    void setUp() throws SQLException {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:branddb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        dataSource = h2DataSource;

        brandDAO = new BrandDAOImpl(dataSource);

        createSchema();
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
        }
    }


    private void createSchema() throws SQLException {
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE Brand (" + "brand_id INT AUTO_INCREMENT PRIMARY KEY, " + "name VARCHAR(100) NOT NULL" + ")");
        }
    }

    private Brand createNewBrand() {
        Brand brand = new Brand();
        brand.setName("TestBrand");
        return brand;
    }


    @Test
    void testSave_ShouldReturnBrandWithGeneratedId() {
        Brand newBrand = createNewBrand();
        assertNull(newBrand.getBrandId());

        Brand savedBrand = brandDAO.save(newBrand);

        assertNotNull(savedBrand.getBrandId());
        assertTrue(savedBrand.getBrandId() > 0);
        assertEquals("TestBrand", savedBrand.getName());
    }

    @Test
    void testFindById_Success() {
        Brand savedBrand = brandDAO.save(createNewBrand());
        int id = savedBrand.getBrandId();

        Optional<Brand> foundBrand = brandDAO.findById(id);

        assertTrue(foundBrand.isPresent());
        assertEquals("TestBrand", foundBrand.get().getName());
    }

    @Test
    void testFindById_NotFound() {
        Optional<Brand> foundBrand = brandDAO.findById(999);
        assertFalse(foundBrand.isPresent());
    }

    @Test
    void testFindAll() {
        Brand b1 = createNewBrand();
        b1.setName("Brand A");
        brandDAO.save(b1);

        Brand b2 = createNewBrand();
        b2.setName("Brand B");
        brandDAO.save(b2);

        List<Brand> brands = brandDAO.findAll();

        assertEquals(2, brands.size());
        assertTrue(brands.stream().anyMatch(b -> b.getName().equals("Brand A")));
    }

    @Test
    void testUpdate() {
        Brand brand = brandDAO.save(createNewBrand());
        int id = brand.getBrandId();

        brand.setName("Updated Brand Name");

        Brand updatedResult = brandDAO.update(brand);

        assertEquals("Updated Brand Name", updatedResult.getName());

        Optional<Brand> dbBrand = brandDAO.findById(id);
        assertTrue(dbBrand.isPresent());
        assertEquals("Updated Brand Name", dbBrand.get().getName());
    }

    @Test
    void testDelete() {
        Brand savedBrand = brandDAO.save(createNewBrand());
        int id = savedBrand.getBrandId();

        brandDAO.delete(id);

        Optional<Brand> foundBrand = brandDAO.findById(id);
        assertFalse(foundBrand.isPresent());
    }
}