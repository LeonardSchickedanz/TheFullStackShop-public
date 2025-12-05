package yorku.thefullstackshop.daos.implementations;

import yorku.thefullstackshop.daos.DatabaseConfig;
import yorku.thefullstackshop.daos.interfaces.BrandDAO;
import yorku.thefullstackshop.models.Brand;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BrandDAOImpl implements BrandDAO {

    private final DataSource dataSource;

    public BrandDAOImpl() {
        this.dataSource = DatabaseConfig.getDataSource();
    }

    public BrandDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @Override
    public Brand save(Brand brand) {
        String sql = "INSERT INTO Brand (name) VALUES (?);";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, brand.getName());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    brand.setBrandId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error while saving brand: " + e.getMessage());
            throw new RuntimeException("Could not save brand to the database.", e);
        }
        return brand;
    }

    @Override
    public Brand update(Brand brand) {
        String sql = "UPDATE Brand SET name = ? WHERE brand_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, brand.getName());
            ps.setInt(2, brand.getBrandId());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error while updating brand: " + e.getMessage());
            throw new RuntimeException("Could not update brand in the database.", e);
        }
        return brand;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM Brand WHERE brand_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error while deleting brand: " + e.getMessage());
            throw new RuntimeException("Could not delete brand from the database.", e);
        }
    }

    @Override
    public Optional<Brand> findById(int id) {
        String sql = "SELECT * FROM Brand WHERE brand_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Brand brand = new Brand();
                    brand.setBrandId(rs.getInt("brand_id"));
                    brand.setName(rs.getString("name"));
                    return Optional.of(brand);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching brand: " + e.getMessage());
            throw new RuntimeException("Could not fetch brand from the database.", e);
        }
    }

    @Override
    public List<Brand> findAll() {
        String sql = "SELECT * FROM Brand;";
        List<Brand> brands = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Brand brand = new Brand();
                brand.setBrandId(rs.getInt("brand_id"));
                brand.setName(rs.getString("name"));
                brands.add(brand);
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching all brands: " + e.getMessage());
            throw new RuntimeException("Could not fetch brands from the database.", e);
        }
        return brands;
    }
}