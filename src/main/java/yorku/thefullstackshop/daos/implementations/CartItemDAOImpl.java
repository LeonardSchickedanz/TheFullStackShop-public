package yorku.thefullstackshop.daos.implementations;

import yorku.thefullstackshop.daos.DatabaseConfig;
import yorku.thefullstackshop.daos.interfaces.CartItemDAO;
import yorku.thefullstackshop.models.CartItem;
import yorku.thefullstackshop.models.Product;
import yorku.thefullstackshop.models.ShoppingCart;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CartItemDAOImpl implements CartItemDAO {

    private final DataSource dataSource;

    public CartItemDAOImpl() {
        this.dataSource = DatabaseConfig.getDataSource();
    }

    public CartItemDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @Override
    public CartItem save(CartItem cartItem) {
        String sql = "INSERT INTO CartItem (quantity, shopping_cart_id, product_id) VALUES (?, ?, ?);";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, cartItem.getQuantity());
            ps.setInt(2, cartItem.getShoppingCart().getShoppingCartId());
            ps.setInt(3, cartItem.getProduct().getProductId());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    cartItem.setCartItemId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error while saving cart item: " + e.getMessage());
            throw new RuntimeException("Could not save cart item to the database.", e);
        }
        return cartItem;
    }

    @Override
    public CartItem update(CartItem cartItem) {
        String sql = "UPDATE CartItem SET quantity = ?, shopping_cart_id = ?, product_id = ? WHERE cart_item_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, cartItem.getQuantity());
            ps.setInt(2, cartItem.getShoppingCart().getShoppingCartId());
            ps.setInt(3, cartItem.getProduct().getProductId());
            ps.setInt(4, cartItem.getCartItemId());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error while updating cart item: " + e.getMessage());
            throw new RuntimeException("Could not update cart item in the database.", e);
        }
        return cartItem;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM CartItem WHERE cart_item_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error while deleting cart item: " + e.getMessage());
            throw new RuntimeException("Could not delete cart item from the database.", e);
        }
    }

    @Override
    public Optional<CartItem> findById(int id) {
        String sql = "SELECT * FROM CartItem WHERE cart_item_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CartItem cartItem = new CartItem();
                    cartItem.setCartItemId(rs.getInt("cart_item_id"));
                    cartItem.setQuantity(rs.getInt("quantity"));

                    ShoppingCart shoppingCart = new ShoppingCart();
                    shoppingCart.setShoppingCartId(rs.getInt("shopping_cart_id"));
                    cartItem.setShoppingCart(shoppingCart);

                    Product product = new Product();
                    product.setProductId(rs.getInt("product_id"));
                    cartItem.setProduct(product);

                    return Optional.of(cartItem);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching cart item: " + e.getMessage());
            throw new RuntimeException("Could not fetch cart item from the database.", e);
        }
    }

    @Override
    public List<CartItem> findAll() {
        String sql = "SELECT * FROM CartItem;";
        List<CartItem> cartItems = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                CartItem cartItem = new CartItem();
                cartItem.setCartItemId(rs.getInt("cart_item_id"));
                cartItem.setQuantity(rs.getInt("quantity"));

                ShoppingCart shoppingCart = new ShoppingCart();
                shoppingCart.setShoppingCartId(rs.getInt("shopping_cart_id"));
                cartItem.setShoppingCart(shoppingCart);

                Product product = new Product();
                product.setProductId(rs.getInt("product_id"));
                cartItem.setProduct(product);

                cartItems.add(cartItem);
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching all cart items: " + e.getMessage());
            throw new RuntimeException("Could not fetch cart items from the database.", e);
        }
        return cartItems;
    }

    @Override
    public List<CartItem> findAllByShoppingCartId(int shoppingCartId) {
        String sql = "SELECT ci.cart_item_id, ci.quantity, ci.shopping_cart_id, " + "       p.product_id, p.name, p.price, p.description, p.img_url, p.quantity as product_stock, p.category_id, " + "       b.brand_id, b.name as brand_name " + "FROM CartItem ci " + "JOIN Product p ON ci.product_id = p.product_id " + "JOIN Brand b ON p.brand_id = b.brand_id " + "WHERE ci.shopping_cart_id = ?";

        List<CartItem> items = new ArrayList<>();

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, shoppingCartId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setCartItemId(rs.getInt("cart_item_id"));
                    item.setQuantity(rs.getInt("quantity"));

                    yorku.thefullstackshop.models.ShoppingCart cart = new yorku.thefullstackshop.models.ShoppingCart();
                    cart.setShoppingCartId(shoppingCartId);
                    item.setShoppingCart(cart);

                    yorku.thefullstackshop.models.Product product = new yorku.thefullstackshop.models.Product();
                    product.setProductId(rs.getInt("product_id"));
                    product.setName(rs.getString("name"));
                    product.setPrice(rs.getBigDecimal("price"));
                    product.setDescription(rs.getString("description"));
                    product.setImgUrl(rs.getString("img_url"));
                    product.setQuantity(rs.getInt("product_stock"));

                    yorku.thefullstackshop.models.Brand brand = new yorku.thefullstackshop.models.Brand();
                    brand.setBrandId(rs.getInt("brand_id"));
                    brand.setName(rs.getString("brand_name"));
                    product.setBrand(brand);

                    yorku.thefullstackshop.models.Category category = new yorku.thefullstackshop.models.Category();
                    category.setCategoryId(rs.getInt("category_id"));

                    product.setCategory(category);

                    item.setProduct(product);

                    items.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching cart items with details: " + e.getMessage());
            throw new RuntimeException("Could not fetch cart items.", e);
        }
        return items;
    }
}