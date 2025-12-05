package yorku.thefullstackshop.daos.implementations;

import yorku.thefullstackshop.daos.DatabaseConfig;
import yorku.thefullstackshop.daos.interfaces.AddressDAO;
import yorku.thefullstackshop.models.Address;
import yorku.thefullstackshop.models.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AddressDAOImpl implements AddressDAO {

    private final DataSource dataSource;

    public AddressDAOImpl() {
        this.dataSource = DatabaseConfig.getDataSource();
    }

    public AddressDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @Override
    public Address save(Address address) {
        String sql = "INSERT INTO Address (street, street_number, postal_code, city, province, country, user_id) VALUES (?, ?, ?, ?, ?, ?, ?);";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, address.getStreet());
            ps.setString(2, address.getStreetNumber());
            ps.setString(3, address.getPostalCode());
            ps.setString(4, address.getCity());

            ps.setString(5, address.getProvince());
            ps.setString(6, address.getCountry());

            ps.setInt(7, address.getUser().getUserId());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    address.setAddressId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error while saving address: " + e.getMessage());
            throw new RuntimeException("Could not save address to the database.", e);
        }
        return address;
    }

    @Override
    public Address update(Address address) {
        String sql = "UPDATE Address SET street = ?, street_number = ?, postal_code = ?, city = ?, province = ?, country = ?, user_id = ? WHERE address_id = ?;";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, address.getStreet());
            ps.setString(2, address.getStreetNumber());
            ps.setString(3, address.getPostalCode());
            ps.setString(4, address.getCity());

            ps.setString(5, address.getProvince());
            ps.setString(6, address.getCountry());

            ps.setInt(7, address.getUser().getUserId());
            ps.setInt(8, address.getAddressId());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error while updating address: " + e.getMessage());
            throw new RuntimeException("Could not update address in the database.", e);
        }
        return address;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM Address WHERE address_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error while deleting address: " + e.getMessage());
            throw new RuntimeException("Could not delete address from the database.", e);
        }
    }

    @Override
    public Optional<Address> findById(int id) {
        String sql = "SELECT * FROM Address WHERE address_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAddress(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching address: " + e.getMessage());
            throw new RuntimeException("Could not fetch address from the database.", e);
        }
    }

    @Override
    public List<Address> findAll() {
        String sql = "SELECT * FROM Address;";
        List<Address> addresses = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                addresses.add(mapResultSetToAddress(rs));
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching all addresses: " + e.getMessage());
            throw new RuntimeException("Could not fetch addresses from the database.", e);
        }
        return addresses;
    }

    @Override
    public Optional<Address> findLatestAddressByUserId(int userId) {
        String sql = "SELECT * FROM Address WHERE user_id = ? ORDER BY last_used DESC LIMIT 1;";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAddress(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private Address mapResultSetToAddress(ResultSet rs) throws SQLException {
        Address address = new Address();
        address.setAddressId(rs.getInt("address_id"));
        address.setStreet(rs.getString("street"));
        address.setStreetNumber(rs.getString("street_number"));
        address.setPostalCode(rs.getString("postal_code"));
        address.setCity(rs.getString("city"));

        address.setProvince(rs.getString("province"));
        address.setCountry(rs.getString("country"));

        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        address.setUser(user);

        return address;
    }
}