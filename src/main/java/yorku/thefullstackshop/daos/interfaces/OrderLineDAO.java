package yorku.thefullstackshop.daos.interfaces;

import yorku.thefullstackshop.models.OrderLine;

import java.util.List;

public interface OrderLineDAO extends BaseDAO<OrderLine> {
    List<OrderLine> findAllByOrderId(int orderId);
}
