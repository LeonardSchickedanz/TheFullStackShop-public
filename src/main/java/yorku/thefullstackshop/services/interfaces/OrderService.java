package yorku.thefullstackshop.services.interfaces;

import yorku.thefullstackshop.models.Address;
import yorku.thefullstackshop.models.Order;
import yorku.thefullstackshop.models.ShoppingCart;
import yorku.thefullstackshop.models.User;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    List<Order> getOrdersByUserId(int userId);

    List<Order> getSalesHistory(String email, String product, String dateFrom, String dateTo);

    Optional<Order> getOrderById(int id);

    Order placeOrderWithAddress(User user, ShoppingCart cart, String cardNumber, Address address) throws Exception;
}