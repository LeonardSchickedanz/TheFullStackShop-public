package yorku.thefullstackshop.daos.implementations;

import yorku.thefullstackshop.daos.DatabaseConfig;
import yorku.thefullstackshop.daos.interfaces.OrderLineDAO;
import yorku.thefullstackshop.models.Order;
import yorku.thefullstackshop.models.OrderLine;
import yorku.thefullstackshop.models.Product;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderLineDAOImpl implements OrderLineDAO {

    private final DataSource dataSource;

    public OrderLineDAOImpl() {
        this.dataSource = DatabaseConfig.getDataSource();
    }

    public OrderLineDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @Override
    public OrderLine save(OrderLine orderLine) {
        String sql = "INSERT INTO OrderLine (quantity, unit_price, order_id, product_id) VALUES (?, ?, ?, ?);";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, orderLine.getQuantity());
            ps.setBigDecimal(2, orderLine.getUnitPrice());
            ps.setInt(3, orderLine.getOrder().getOrderId());
            ps.setInt(4, orderLine.getProduct().getProductId());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    orderLine.setOrderLineId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error while saving order line: " + e.getMessage());
            throw new RuntimeException("Could not save order line to the database.", e);
        }
        return orderLine;
    }

    @Override
    public OrderLine update(OrderLine orderLine) {
        String sql = "UPDATE OrderLine SET quantity = ?, unit_price = ?, order_id = ?, product_id = ? WHERE order_line_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderLine.getQuantity());
            ps.setBigDecimal(2, orderLine.getUnitPrice());
            ps.setInt(3, orderLine.getOrder().getOrderId());
            ps.setInt(4, orderLine.getProduct().getProductId());
            ps.setInt(5, orderLine.getOrderLineId());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error while updating order line: " + e.getMessage());
            throw new RuntimeException("Could not update order line in the database.", e);
        }
        return orderLine;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM OrderLine WHERE order_line_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error while deleting order line: " + e.getMessage());
            throw new RuntimeException("Could not delete order line from the database.", e);
        }
    }

    @Override
    public Optional<OrderLine> findById(int id) {
        String sql = "SELECT * FROM OrderLine WHERE order_line_id = ?;";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    OrderLine orderLine = new OrderLine();
                    orderLine.setOrderLineId(rs.getInt("order_line_id"));
                    orderLine.setQuantity(rs.getInt("quantity"));
                    orderLine.setUnitPrice(rs.getBigDecimal("unit_price"));

                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    orderLine.setOrder(order);

                    Product product = new Product();
                    product.setProductId(rs.getInt("product_id"));
                    orderLine.setProduct(product);

                    return Optional.of(orderLine);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching order line: " + e.getMessage());
            throw new RuntimeException("Could not fetch order line from the database.", e);
        }
    }

    @Override
    public List<OrderLine> findAll() {
        String sql = "SELECT * FROM OrderLine;";
        List<OrderLine> orderLines = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                OrderLine orderLine = new OrderLine();
                orderLine.setOrderLineId(rs.getInt("order_line_id"));
                orderLine.setQuantity(rs.getInt("quantity"));
                orderLine.setUnitPrice(rs.getBigDecimal("unit_price"));

                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                orderLine.setOrder(order);

                Product product = new Product();
                product.setProductId(rs.getInt("product_id"));
                orderLine.setProduct(product);

                orderLines.add(orderLine);
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching all order lines: " + e.getMessage());
            throw new RuntimeException("Could not fetch order lines from the database.", e);
        }
        return orderLines;
    }

    @Override
    public List<OrderLine> findAllByOrderId(int orderId) {
        String sql =
                "SELECT ol.order_line_id, ol.quantity, ol.unit_price, " +
                        "       p.product_id, p.name, p.img_url, " +
                        "       b.brand_id, b.name as brand_name " +
                        "FROM OrderLine ol " +
                        "JOIN Product p ON ol.product_id = p.product_id " +
                        "JOIN Brand b ON p.brand_id = b.brand_id " +
                        "WHERE ol.order_id = ?";

        List<OrderLine> lines = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderLine line = new OrderLine();
                    line.setOrderLineId(rs.getInt("order_line_id"));
                    line.setQuantity(rs.getInt("quantity"));
                    line.setUnitPrice(rs.getBigDecimal("unit_price"));

                    yorku.thefullstackshop.models.Order order = new yorku.thefullstackshop.models.Order();
                    order.setOrderId(orderId);
                    line.setOrder(order);

                    yorku.thefullstackshop.models.Product product = new yorku.thefullstackshop.models.Product();
                    product.setProductId(rs.getInt("product_id"));
                    product.setName(rs.getString("name"));
                    product.setImgUrl(rs.getString("img_url"));

                    yorku.thefullstackshop.models.Brand brand = new yorku.thefullstackshop.models.Brand();
                    brand.setName(rs.getString("brand_name"));
                    product.setBrand(brand);

                    line.setProduct(product);

                    lines.add(line);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not fetch order lines.", e);
        }
        return lines;
    }
}