package yorku.thefullstackshop.daos.implementations;

import yorku.thefullstackshop.daos.DatabaseConfig;
import yorku.thefullstackshop.daos.interfaces.ProductDAO;
import yorku.thefullstackshop.models.Brand;
import yorku.thefullstackshop.models.Category;
import yorku.thefullstackshop.models.Product;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDAOImpl implements ProductDAO {

    private final DataSource dataSource;

    public ProductDAOImpl() {
        this.dataSource = DatabaseConfig.getDataSource();
    }

    public ProductDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @Override
    public List<Product> findWithFilters(String searchQuery, Integer brandId, Integer categoryId, String sortOrder) {
        StringBuilder sql = new StringBuilder("SELECT p.*, b.name as brand_name, c.name as category_name FROM Product p ");
        sql.append("JOIN Brand b ON p.brand_id = b.brand_id ");
        sql.append("JOIN Category c ON p.category_id = c.category_id ");
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql.append("AND (p.name LIKE ? OR p.description LIKE ?) ");
            String likeQuery = "%" + searchQuery + "%";
            params.add(likeQuery);
            params.add(likeQuery);
        }

        if (brandId != null) {
            sql.append("AND p.brand_id = ? ");
            params.add(brandId);
        }

        if (categoryId != null) {
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
        }

        if (sortOrder != null) {
            switch (sortOrder) {
                case "price_asc":
                    sql.append("ORDER BY p.price ASC");
                    break;
                case "price_desc":
                    sql.append("ORDER BY p.price DESC");
                    break;
                case "name_asc":
                    sql.append("ORDER BY p.name ASC");
                    break;
                case "name_desc":
                    sql.append("ORDER BY p.name DESC");
                    break;
                default:
                    sql.append("ORDER BY p.product_id DESC");
            }
        } else {
            sql.append("ORDER BY p.product_id DESC");
        }

        List<Product> products = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public List<Brand> findAllBrands() {
        List<Brand> brands = new ArrayList<>();
        String sql = "SELECT * FROM Brand ORDER BY name ASC";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Brand b = new Brand();
                b.setBrandId(rs.getInt("brand_id"));
                b.setName(rs.getString("name"));
                brands.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return brands;
    }

    @Override
    public List<Category> findAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM Category ORDER BY name ASC";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Category c = new Category();
                c.setCategoryId(rs.getInt("category_id"));
                c.setName(rs.getString("name"));
                categories.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    @Override
    public Product save(Product entity) {
        return null;
    }

    @Override
    public Product update(Product entity) {
        return null;
    }

    @Override
    public void delete(int id) {
    }

    @Override
    public Optional<Product> findById(int id) {
        String sql = "SELECT p.*, b.name as brand_name, c.name as category_name FROM Product p " +
                "JOIN Brand b ON p.brand_id = b.brand_id " +
                "JOIN Category c ON p.category_id = c.category_id " +
                "WHERE p.product_id = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Product> findAll() {
        return findWithFilters(null, null, null, null);
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setImgUrl(rs.getString("img_url"));
        p.setQuantity(rs.getInt("quantity"));

        Brand b = new Brand();
        b.setBrandId(rs.getInt("brand_id"));
        b.setName(rs.getString("brand_name"));
        p.setBrand(b);

        Category c = new Category();
        c.setCategoryId(rs.getInt("category_id"));
        c.setName(rs.getString("category_name"));
        p.setCategory(c);

        return p;
    }

    @Override
    public int updateQuantity(int productId, int newQuantity) {
        String sql = "UPDATE Product SET quantity = ? WHERE product_id = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, productId);
            ps.executeUpdate();
            return newQuantity;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not update product quantity.", e);
        }
    }
}