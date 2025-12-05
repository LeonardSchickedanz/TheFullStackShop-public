package yorku.thefullstackshop.services.interfaces;

import yorku.thefullstackshop.models.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    public ShoppingCart getCart(Integer userId, String sessionId);

    public void addItemToCart(Integer userId, String sessionId, int productId, int quantity);

    public void removeFromCart(Integer userId, String sessionId, int productId);

    public void updateItemQuantity(Integer userId, String sessionId, int productId, int quantity);

    int getCartItemCount(Integer userId, String sessionId);

    public void clearCart(Integer userId, String sessionId);

    void mergeCarts(Integer userId, String sessionId);

    List<String> validateCartInventory(ShoppingCart cart, String sessionId);
}