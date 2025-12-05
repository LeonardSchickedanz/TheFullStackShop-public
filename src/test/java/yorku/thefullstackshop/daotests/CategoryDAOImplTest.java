package yorku.thefullstackshop.daotests;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yorku.thefullstackshop.daos.implementations.CategoryDAOImpl;
import yorku.thefullstackshop.models.Category;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CategoryDAOImplTest {

    private CategoryDAOImpl categoryDAO;
    private DataSource dataSource;

    @BeforeEach
    void setUp() throws SQLException {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:categorydb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        dataSource = h2DataSource;

        categoryDAO = new CategoryDAOImpl(dataSource);

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

            stmt.execute("CREATE TABLE Category (" + "category_id INT AUTO_INCREMENT PRIMARY KEY, " + "name VARCHAR(100) NOT NULL" + ")");
        }
    }

    private Category createNewCategory() {
        Category category = new Category();
        category.setName("Electronics");
        return category;
    }

    @Test
    void testSave_ShouldReturnCategoryWithGeneratedId() {
        Category newCategory = createNewCategory();
        assertNull(newCategory.getCategoryId());

        Category savedCategory = categoryDAO.save(newCategory);

        assertNotNull(savedCategory.getCategoryId());
        assertTrue(savedCategory.getCategoryId() > 0);
        assertEquals("Electronics", savedCategory.getName());
    }

    @Test
    void testFindById_Success() {
        Category savedCategory = categoryDAO.save(createNewCategory());
        int id = savedCategory.getCategoryId();

        Optional<Category> foundCategory = categoryDAO.findById(id);

        assertTrue(foundCategory.isPresent());
        assertEquals("Electronics", foundCategory.get().getName());
    }

    @Test
    void testFindById_NotFound() {
        Optional<Category> foundCategory = categoryDAO.findById(999);
        assertFalse(foundCategory.isPresent());
    }

    @Test
    void testFindAll() {
        Category c1 = createNewCategory();
        c1.setName("Books");
        categoryDAO.save(c1);

        Category c2 = createNewCategory();
        c2.setName("Clothing");
        categoryDAO.save(c2);

        List<Category> categories = categoryDAO.findAll();

        assertEquals(2, categories.size());
        assertTrue(categories.stream().anyMatch(c -> c.getName().equals("Books")));
    }

    @Test
    void testUpdate() {
        Category category = categoryDAO.save(createNewCategory());
        int id = category.getCategoryId();

        category.setName("Updated Category Name");

        Category updatedResult = categoryDAO.update(category);

        assertEquals("Updated Category Name", updatedResult.getName());

        Optional<Category> dbCategory = categoryDAO.findById(id);
        assertTrue(dbCategory.isPresent());
        assertEquals("Updated Category Name", dbCategory.get().getName());
    }

    @Test
    void testDelete() {
        Category savedCategory = categoryDAO.save(createNewCategory());
        int id = savedCategory.getCategoryId();

        categoryDAO.delete(id);

        Optional<Category> foundCategory = categoryDAO.findById(id);
        assertFalse(foundCategory.isPresent());
    }
}