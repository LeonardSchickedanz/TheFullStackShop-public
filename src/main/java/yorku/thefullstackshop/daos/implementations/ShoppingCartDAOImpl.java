package yorku.thefullstackshop.daos.implementations;

import yorku.thefullstackshop.daos.DatabaseConfig;
import yorku.thefullstackshop.daos.interfaces.ShoppingCartDAO;
import yorku.thefullstackshop.models.ShoppingCart;
import yorku.thefullstackshop.models.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShoppingCartDAOImpl implements ShoppingCartDAO {

    private final DataSource dataSource;

    public ShoppingCartDAOImpl() {
        this.dataSource = DatabaseConfig.getDataSource();
    }

    public ShoppingCartDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @Override
    public ShoppingCart save(ShoppingCart shoppingCart) {
        String sql = "INSERT INTO ShoppingCart (user_id, session_id) VALUES (?, ?);";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (shoppingCart.getUser() != null) {
                ps.setInt(1, shoppingCart.getUser().getUserId());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }

            ps.setString(2, shoppingCart.getSessionId());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    shoppingCart.setShoppingCartId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error while saving shopping cart: " + e.getMessage());
            throw new RuntimeException("Could not save shopping cart to the database.", e);
        }
        return shoppingCart;
    }

    @Override
    public ShoppingCart update(ShoppingCart shoppingCart) {
        String sql = "UPDATE ShoppingCart SET user_id = ?, session_id = ? WHERE shopping_cart_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            if (shoppingCart.getUser() != null) {
                ps.setInt(1, shoppingCart.getUser().getUserId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }

            ps.setString(2, shoppingCart.getSessionId());
            ps.setInt(3, shoppingCart.getShoppingCartId());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error while updating shopping cart: " + e.getMessage());
            throw new RuntimeException("Could not update shopping cart in the database.", e);
        }
        return shoppingCart;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM ShoppingCart WHERE shopping_cart_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error while deleting shopping cart: " + e.getMessage());
            throw new RuntimeException("Could not delete shopping cart from the database.", e);
        }
    }

    @Override
    public Optional<ShoppingCart> findById(int id) {
        String sql = "SELECT * FROM ShoppingCart WHERE shopping_cart_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCart(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching shopping cart: " + e.getMessage());
            throw new RuntimeException("Could not fetch shopping cart from the database.", e);
        }
    }

    @Override
    public List<ShoppingCart> findAll() {
        String sql = "SELECT * FROM ShoppingCart;";
        List<ShoppingCart> shoppingCarts = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                shoppingCarts.add(mapResultSetToCart(rs));
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching all shopping carts: " + e.getMessage());
            throw new RuntimeException("Could not fetch shopping carts from the database.", e);
        }
        return shoppingCarts;
    }

    private ShoppingCart mapResultSetToCart(ResultSet rs) throws SQLException {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setShoppingCartId(rs.getInt("shopping_cart_id"));

        shoppingCart.setSessionId(rs.getString("session_id"));

        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            User user = new User();
            user.setUserId(userId);
            shoppingCart.setUser(user);
        }

        return shoppingCart;
    }
}