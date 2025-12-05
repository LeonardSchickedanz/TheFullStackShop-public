package yorku.thefullstackshop.services.implementations;

import yorku.thefullstackshop.daos.implementations.CartItemDAOImpl;
import yorku.thefullstackshop.daos.implementations.OrderDAOImpl;
import yorku.thefullstackshop.daos.implementations.OrderLineDAOImpl;
import yorku.thefullstackshop.daos.implementations.ProductDAOImpl;
import yorku.thefullstackshop.daos.interfaces.CartItemDAO;
import yorku.thefullstackshop.daos.interfaces.OrderDAO;
import yorku.thefullstackshop.daos.interfaces.OrderLineDAO;
import yorku.thefullstackshop.daos.interfaces.ProductDAO;
import yorku.thefullstackshop.models.*;
import yorku.thefullstackshop.services.interfaces.OrderService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class OrderServiceImpl implements OrderService {

    private final OrderDAO orderDAO;
    private final OrderLineDAO orderLineDAO;
    private final ProductDAO productDAO;
    private final CartItemDAO cartItemDAO;

    public OrderServiceImpl(OrderDAOImpl orderDAO, OrderLineDAOImpl orderLineDAO, ProductDAOImpl productDAO, CartItemDAOImpl cartItemDAO) {
        this.orderDAO = orderDAO;
        this.orderLineDAO = orderLineDAO;
        this.productDAO = productDAO;
        this.cartItemDAO = cartItemDAO;
    }

    public OrderServiceImpl() {
        this.orderDAO = new OrderDAOImpl();
        this.orderLineDAO = new OrderLineDAOImpl();
        this.productDAO = new ProductDAOImpl();
        this.cartItemDAO = new CartItemDAOImpl();
    }

    @Override
    public List<Order> getOrdersByUserId(int userId) {
        return orderDAO.findAllByUserId(userId);
    }

    @Override
    public List<Order> getSalesHistory(String email, String product, String dateFrom, String dateTo) {
        return orderDAO.findSalesHistory(email, product, dateFrom, dateTo);
    }

    @Override
    public Optional<Order> getOrderById(int id) {
        Optional<Order> orderOpt = orderDAO.findById(id);

        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            List<OrderLine> lines = orderLineDAO.findAllByOrderId(id);
            order.setOrderLines(new java.util.HashSet<>(lines));
            return Optional.of(order);
        }
        return Optional.empty();
    }

    @Override
    public Order placeOrderWithAddress(User user, ShoppingCart cart, String cardNumber, Address address) throws Exception {
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : cart.getCartItems()) {
            Product p = item.getProduct();
            if (p.getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Product " + p.getName() + " is out of stock.");
            }
            BigDecimal lineTotal = p.getPrice().multiply(new BigDecimal(item.getQuantity()));
            total = total.add(lineTotal);
        }

        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setTotalPrice(total);
        order = orderDAO.save(order);

        for (CartItem item : cart.getCartItems()) {
            OrderLine line = new OrderLine();
            line.setOrder(order);
            line.setProduct(item.getProduct());
            line.setQuantity(item.getQuantity());
            line.setUnitPrice(item.getProduct().getPrice());
            orderLineDAO.save(line);

            Product p = item.getProduct();
            int newQuantity = p.getQuantity() - item.getQuantity();
            productDAO.updateQuantity(p.getProductId(), newQuantity);

            cartItemDAO.delete(item.getCartItemId());
        }

        return order;
    }
}