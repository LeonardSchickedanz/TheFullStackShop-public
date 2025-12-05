package yorku.thefullstackshop.services.interfaces;

import yorku.thefullstackshop.models.Order;
import yorku.thefullstackshop.models.User;

import java.util.List;
import java.util.Optional;

public interface AdminService {
    List<Order> getSalesHistory(String email, String product, String dateFrom, String dateTo);

    List<User> getAllCustomers();

    Optional<User> getCustomerById(int id);

    User updateCustomer(User user);

    int updateInventory(int productId, int newQuantity);

    List<User> getAllCustomers(String searchEmail);
}
