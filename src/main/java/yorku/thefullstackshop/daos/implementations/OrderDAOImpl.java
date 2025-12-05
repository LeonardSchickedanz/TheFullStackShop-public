package yorku.thefullstackshop.daos.implementations;

import yorku.thefullstackshop.daos.DatabaseConfig;
import yorku.thefullstackshop.daos.interfaces.OrderDAO;
import yorku.thefullstackshop.models.Address;
import yorku.thefullstackshop.models.Order;
import yorku.thefullstackshop.models.User;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDAOImpl implements OrderDAO {

    private final DataSource dataSource;

    public OrderDAOImpl() {
        this.dataSource = DatabaseConfig.getDataSource();
    }

    public OrderDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @Override
    public Order save(Order order) {
        String sql = "INSERT INTO `Order` (transaction_time, total_price, user_id, address_id) VALUES (?, ?, ?, ?);";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (order.getTransactionTime() == null) {
                order.setTransactionTime(LocalDateTime.now());
            }

            ps.setTimestamp(1, Timestamp.valueOf(order.getTransactionTime()));
            ps.setBigDecimal(2, order.getTotalPrice());
            ps.setInt(3, order.getUser().getUserId());
            ps.setInt(4, order.getAddress().getAddressId());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    order.setOrderId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error while saving order: " + e.getMessage());
            throw new RuntimeException("Could not save order to the database.", e);
        }
        return order;
    }

    @Override
    public Order update(Order order) {
        String sql = "UPDATE `Order` SET transaction_time = ?, total_price = ?, user_id = ?, address_id = ? WHERE order_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(order.getTransactionTime()));
            ps.setBigDecimal(2, order.getTotalPrice());
            ps.setInt(3, order.getUser().getUserId());
            ps.setInt(4, order.getAddress().getAddressId());
            ps.setInt(5, order.getOrderId());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error while updating order: " + e.getMessage());
            throw new RuntimeException("Could not update order in the database.", e);
        }
        return order;
    }

    @Override
    public void delete(int id) {
        String deleteOrderLineSql = "DELETE FROM OrderLine WHERE order_id = ?;";
        String deleteOrderSql = "DELETE FROM `Order` WHERE order_id = ?;";

        try (Connection con = getConnection();
             PreparedStatement psLine = con.prepareStatement(deleteOrderLineSql);
             PreparedStatement psOrder = con.prepareStatement(deleteOrderSql)) {

            psLine.setInt(1, id);
            psLine.executeUpdate();

            psOrder.setInt(1, id);
            psOrder.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Database error while deleting order: " + e.getMessage());
            throw new RuntimeException("Could not delete order from the database.", e);
        }
    }

    @Override
    public Optional<Order> findById(int id) {
        String sql = "SELECT o.*, u.first_name, u.last_name, u.email, " +
                "a.address_id, a.street, a.street_number, a.postal_code, " +
                "a.city, a.province, a.country " +
                "FROM `Order` o " +
                "JOIN `User` u ON o.user_id = u.user_id " +
                "JOIN Address a ON o.address_id = a.address_id " +
                "WHERE o.order_id = ?;";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setTransactionTime(rs.getTimestamp("transaction_time").toLocalDateTime());
                    order.setTotalPrice(rs.getBigDecimal("total_price"));

                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setEmail(rs.getString("email"));
                    order.setUser(user);

                    Address address = new Address();
                    address.setAddressId(rs.getInt("address_id"));
                    address.setStreet(rs.getString("street"));
                    address.setStreetNumber(rs.getString("STREET_NUMBER"));
                    address.setPostalCode(rs.getString("POSTAL_CODE"));
                    address.setCity(rs.getString("CITY"));
                    address.setProvince(rs.getString("PROVINCE"));
                    address.setCountry(rs.getString("COUNTRY"));
                    order.setAddress(address);

                    return Optional.of(order);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching order: " + e.getMessage());
            throw new RuntimeException("Could not fetch order from the database.", e);
        }
    }

    @Override
    public List<Order> findAll() {
        String sql = "SELECT o.*, u.first_name, u.last_name, u.email, " +
                "a.address_id, a.street, a.street_number, a.postal_code, a.city, a.province, a.country " +
                "FROM `Order` o " +
                "JOIN `User` u ON o.user_id = u.user_id " +
                "JOIN Address a ON o.address_id = a.address_id;";

        List<Order> orders = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setTransactionTime(rs.getTimestamp("transaction_time").toLocalDateTime());
                order.setTotalPrice(rs.getBigDecimal("total_price"));

                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                order.setUser(user);

                Address address = new Address();
                address.setAddressId(rs.getInt("address_id"));
                address.setStreet(rs.getString("street"));
                address.setStreetNumber(rs.getString("STREET_NUMBER"));
                address.setPostalCode(rs.getString("POSTAL_CODE"));
                address.setCity(rs.getString("CITY"));
                address.setProvince(rs.getString("PROVINCE"));
                address.setCountry(rs.getString("COUNTRY"));
                order.setAddress(address);

                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching all orders: " + e.getMessage());
            throw new RuntimeException("Could not fetch orders from the database.", e);
        }
        return orders;
    }

    @Override
    public List<Order> findAllByUserId(int userId) {
        String sql = "SELECT o.*, u.first_name, u.last_name, u.email, " +
                "a.address_id, a.street, a.street_number, a.postal_code, " +
                "a.city, a.province, a.country " +
                "FROM `Order` o " +
                "JOIN `User` u ON o.user_id = u.user_id " +
                "JOIN Address a ON o.address_id = a.address_id " +
                "WHERE o.user_id = ? " +
                "ORDER BY o.transaction_time DESC;";

        List<Order> orders = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setTransactionTime(rs.getTimestamp("transaction_time").toLocalDateTime());
                    order.setTotalPrice(rs.getBigDecimal("total_price"));

                    User user = new User();
                    user.setUserId(userId);
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setEmail(rs.getString("email"));
                    order.setUser(user);

                    Address address = new Address();
                    address.setAddressId(rs.getInt("address_id"));
                    address.setStreet(rs.getString("street"));
                    address.setStreetNumber(rs.getString("STREET_NUMBER"));
                    address.setPostalCode(rs.getString("POSTAL_CODE"));
                    address.setCity(rs.getString("CITY"));
                    address.setProvince(rs.getString("PROVINCE"));
                    address.setCountry(rs.getString("COUNTRY"));
                    order.setAddress(address);

                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching user orders: " + e.getMessage());
            throw new RuntimeException("Could not fetch orders.", e);
        }
        return orders;
    }

    @Override
    public List<Order> findSalesHistory(String customerEmail, String productName, String dateFrom, String dateTo) {
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT o.order_id, o.transaction_time, o.total_price, " +
                        "u.email, u.user_id, u.first_name, u.last_name, " +
                        "a.address_id, a.street, a.street_number, a.postal_code, a.city, a.province, a.country " +
                        "FROM `Order` o " +
                        "JOIN `User` u ON o.user_id = u.user_id " +
                        "JOIN Address a ON o.address_id = a.address_id " +
                        "LEFT JOIN OrderLine ol ON o.order_id = ol.order_id " +
                        "LEFT JOIN Product p ON ol.product_id = p.product_id " +
                        "WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (customerEmail != null && !customerEmail.isEmpty()) {
            sql.append("AND u.email LIKE ? ");
            params.add("%" + customerEmail + "%");
        }
        if (productName != null && !productName.isEmpty()) {
            sql.append("AND p.name LIKE ? ");
            params.add("%" + productName + "%");
        }
        if (dateFrom != null && !dateFrom.isEmpty()) {
            sql.append("AND o.transaction_time >= ? ");
            params.add(Timestamp.valueOf(LocalDateTime.parse(dateFrom + "T00:00:00")));
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            sql.append("AND o.transaction_time <= ? ");
            params.add(Timestamp.valueOf(LocalDateTime.parse(dateTo + "T23:59:59")));
        }

        sql.append("ORDER BY o.transaction_time DESC");

        List<Order> orders = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof String) {
                    ps.setString(i + 1, (String) params.get(i));
                } else if (params.get(i) instanceof Timestamp) {
                    ps.setTimestamp(i + 1, (Timestamp) params.get(i));
                } else {
                    ps.setObject(i + 1, params.get(i));
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = new Order();
                    o.setOrderId(rs.getInt("order_id"));
                    o.setTransactionTime(rs.getTimestamp("transaction_time").toLocalDateTime());
                    o.setTotalPrice(rs.getBigDecimal("total_price"));

                    User u = new User();
                    u.setUserId(rs.getInt("user_id"));
                    u.setFirstName(rs.getString("first_name"));
                    u.setLastName(rs.getString("last_name"));
                    u.setEmail(rs.getString("email"));
                    o.setUser(u);

                    Address address = new Address();
                    address.setAddressId(rs.getInt("address_id"));
                    address.setStreet(rs.getString("street"));
                    address.setStreetNumber(rs.getString("STREET_NUMBER"));
                    address.setPostalCode(rs.getString("POSTAL_CODE"));
                    address.setCity(rs.getString("CITY"));
                    address.setProvince(rs.getString("PROVINCE"));
                    address.setCountry(rs.getString("COUNTRY"));
                    o.setAddress(address);

                    orders.add(o);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not fetch sales history from the database.", e);
        }
        return orders;
    }
}