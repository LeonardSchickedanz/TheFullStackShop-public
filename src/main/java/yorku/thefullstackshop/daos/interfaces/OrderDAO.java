package yorku.thefullstackshop.daos.interfaces;

import yorku.thefullstackshop.models.Order;

import java.util.List;

public interface OrderDAO extends BaseDAO<Order> {
    List<Order> findAllByUserId(int userId);

    List<Order> findSalesHistory(String customerEmail, String productName, String dateFrom, String dateTo);
}

