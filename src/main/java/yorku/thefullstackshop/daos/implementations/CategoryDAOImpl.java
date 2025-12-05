package yorku.thefullstackshop.daos.implementations;

import yorku.thefullstackshop.daos.DatabaseConfig;
import yorku.thefullstackshop.daos.interfaces.BaseDAO;
import yorku.thefullstackshop.models.Category;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryDAOImpl implements BaseDAO<Category> {

    private final DataSource dataSource;

    public CategoryDAOImpl() {
        this.dataSource = DatabaseConfig.getDataSource();
    }

    public CategoryDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @Override
    public Category save(Category category) {
        String sql = "INSERT INTO Category (name) VALUES (?);";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, category.getName());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setCategoryId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error while saving category: " + e.getMessage());
            throw new RuntimeException("Could not save category to the database.", e);
        }
        return category;
    }

    @Override
    public Category update(Category category) {
        String sql = "UPDATE Category SET name = ? WHERE category_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, category.getName());
            ps.setInt(2, category.getCategoryId());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error while updating category: " + e.getMessage());
            throw new RuntimeException("Could not update category in the database.", e);
        }
        return category;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM Category WHERE category_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error while deleting category: " + e.getMessage());
            throw new RuntimeException("Could not delete category from the database.", e);
        }
    }

    @Override
    public Optional<Category> findById(int id) {
        String sql = "SELECT * FROM Category WHERE category_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Category category = new Category();
                    category.setCategoryId(rs.getInt("category_id"));
                    category.setName(rs.getString("name"));
                    return Optional.of(category);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching category: " + e.getMessage());
            throw new RuntimeException("Could not fetch category from the database.", e);
        }
    }

    @Override
    public List<Category> findAll() {
        String sql = "SELECT * FROM Category;";
        List<Category> categories = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Category category = new Category();
                category.setCategoryId(rs.getInt("category_id"));
                category.setName(rs.getString("name"));
                categories.add(category);
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching all categories: " + e.getMessage());
            throw new RuntimeException("Could not fetch categories from the database.", e);
        }
        return categories;
    }
}