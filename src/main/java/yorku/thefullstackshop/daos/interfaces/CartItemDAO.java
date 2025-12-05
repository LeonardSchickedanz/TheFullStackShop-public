package yorku.thefullstackshop.daos.interfaces;

import yorku.thefullstackshop.models.CartItem;

import java.util.List;

public interface CartItemDAO extends BaseDAO<CartItem> {
    List<CartItem> findAllByShoppingCartId(int shoppingCartId);
}
