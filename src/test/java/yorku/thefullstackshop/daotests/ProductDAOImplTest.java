package yorku.thefullstackshop.daotests;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yorku.thefullstackshop.daos.implementations.ProductDAOImpl;
import yorku.thefullstackshop.models.Brand;
import yorku.thefullstackshop.models.Category;
import yorku.thefullstackshop.models.Product;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductDAOImplTest {

    private ProductDAOImpl productDAO;
    private DataSource dataSource;

    private final int BRAND_APPLE = 1;
    private final int BRAND_DELL = 2;
    private final int CAT_ELECTRONICS = 10;
    private final int CAT_BOOKS = 20;

    @BeforeEach
    void setUp() throws SQLException {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:productdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        dataSource = h2DataSource;

        productDAO = new ProductDAOImpl(dataSource);
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

            stmt.execute("CREATE TABLE Product (" + "product_id INT PRIMARY KEY, " + "name VARCHAR(150), " + "description TEXT, " + "price DECIMAL(10, 2), " + "img_url VARCHAR(255), " + "quantity INT, " + "brand_id INT, " + "category_id INT, " + "FOREIGN KEY (brand_id) REFERENCES Brand(brand_id), " + "FOREIGN KEY (category_id) REFERENCES Category(category_id)" + ")");

            stmt.execute("INSERT INTO Brand VALUES (" + BRAND_APPLE + ", 'Apple')");
            stmt.execute("INSERT INTO Brand VALUES (" + BRAND_DELL + ", 'Dell')");
            stmt.execute("INSERT INTO Category VALUES (" + CAT_ELECTRONICS + ", 'Electronics')");
            stmt.execute("INSERT INTO Category VALUES (" + CAT_BOOKS + ", 'Books')");

            stmt.execute("INSERT INTO Product VALUES (1, 'MacBook Pro', 'High-end Laptop', 2500.00, 'img1', 10, " + BRAND_APPLE + ", " + CAT_ELECTRONICS + ")");
            stmt.execute("INSERT INTO Product VALUES (2, 'Dell XPS 13', 'Premium Laptop', 1500.00, 'img2', 5, " + BRAND_DELL + ", " + CAT_ELECTRONICS + ")");
            stmt.execute("INSERT INTO Product VALUES (3, 'Java Book', 'Programming Guide', 50.00, 'img3', 20, " + BRAND_DELL + ", " + CAT_BOOKS + ")");
        }
    }


    @Test
    void testFindAllBrands_ReturnsSortedList() {
        List<Brand> brands = productDAO.findAllBrands();

        assertEquals(2, brands.size());
        assertEquals("Apple", brands.get(0).getName());
        assertEquals("Dell", brands.get(1).getName());
    }

    @Test
    void testFindAllCategories_ReturnsSortedList() {
        List<Category> categories = productDAO.findAllCategories();

        assertEquals(2, categories.size());
        assertEquals("Books", categories.get(0).getName());
        assertEquals("Electronics", categories.get(1).getName());
    }

    @Test
    void testFindById_SuccessWithJoins() {
        Optional<Product> pOpt = productDAO.findById(1);

        assertTrue(pOpt.isPresent());
        Product p = pOpt.get();
        assertEquals("MacBook Pro", p.getName());
        assertEquals(BRAND_APPLE, p.getBrand().getBrandId());
        assertEquals("Apple", p.getBrand().getName());
        assertEquals("Electronics", p.getCategory().getName());
    }

    @Test
    void testUpdateQuantity() {
        productDAO.updateQuantity(2, 99);

        Optional<Product> pOpt = productDAO.findById(2);
        assertTrue(pOpt.isPresent());
        assertEquals(99, pOpt.get().getQuantity());
    }


    @Test
    void testFindWithFilters_NoFilters_ReturnsAllSortedByProductIdDesc() {
        List<Product> products = productDAO.findWithFilters(null, null, null, null);

        assertEquals(3, products.size());
        assertEquals("Java Book", products.get(0).getName());
        assertEquals("MacBook Pro", products.get(2).getName());
    }

    @Test
    void testFindWithFilters_FilterByBrand() {
        List<Product> products = productDAO.findWithFilters(null, BRAND_APPLE, null, null);

        assertEquals(1, products.size());
        assertEquals("MacBook Pro", products.get(0).getName());
    }

    @Test
    void testFindWithFilters_FilterByCategory() {
        List<Product> products = productDAO.findWithFilters(null, null, CAT_BOOKS, null);

        assertEquals(1, products.size());
        assertEquals("Java Book", products.get(0).getName());
    }

    @Test
    void testFindWithFilters_SearchByNameAndDescription() {
        List<Product> products = productDAO.findWithFilters("Laptop", null, null, null);
        assertEquals(2, products.size());

        products = productDAO.findWithFilters("Guide", null, null, null);
        assertEquals(1, products.size());
    }

    @Test
    void testFindWithFilters_SortByPriceAsc() {
        List<Product> products = productDAO.findWithFilters(null, null, null, "price_asc");

        assertEquals(3, products.size());
        assertEquals("Java Book", products.get(0).getName());
        assertEquals("MacBook Pro", products.get(2).getName());
    }

    @Test
    void testFindWithFilters_SortByNameDesc() {
        List<Product> products = productDAO.findWithFilters(null, null, null, "name_desc");

        assertEquals(3, products.size());
        assertEquals("MacBook Pro", products.get(0).getName());
        assertEquals("Dell XPS 13", products.get(2).getName());

        assertEquals("Java Book", products.get(1).getName());
    }
}