package yorku.thefullstackshop.services.implementations;

import yorku.thefullstackshop.daos.implementations.CartItemDAOImpl;
import yorku.thefullstackshop.daos.implementations.ProductDAOImpl;
import yorku.thefullstackshop.daos.implementations.ShoppingCartDAOImpl;
import yorku.thefullstackshop.models.CartItem;
import yorku.thefullstackshop.models.Product;
import yorku.thefullstackshop.models.ShoppingCart;
import yorku.thefullstackshop.models.User;
import yorku.thefullstackshop.services.interfaces.ShoppingCartService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartDAOImpl cartDAO;
    private final CartItemDAOImpl cartItemDAO;
    private final ProductDAOImpl productDAO;

    public ShoppingCartServiceImpl(ShoppingCartDAOImpl cartDAO, CartItemDAOImpl cartItemDAO, ProductDAOImpl productDAO) {
        this.cartDAO = cartDAO;
        this.cartItemDAO = cartItemDAO;
        this.productDAO = productDAO;
    }

    public ShoppingCartServiceImpl() {
        this.cartDAO = new ShoppingCartDAOImpl();
        this.cartItemDAO = new CartItemDAOImpl();
        this.productDAO = new ProductDAOImpl();
    }

    @Override
    public ShoppingCart getCart(Integer userId, String sessionId) {
        List<ShoppingCart> allCarts = cartDAO.findAll();
        ShoppingCart userCart = null;
        ShoppingCart guestCart = null;

        for (ShoppingCart c : allCarts) {
            if (userId != null && c.getUser() != null && c.getUser().getUserId().equals(userId)) {
                userCart = c;
            } else if (sessionId != null && sessionId.equals(c.getSessionId()) && (c.getUser() == null || c.getUser().getUserId() == 0)) {
                guestCart = c;
            }
        }

        ShoppingCart finalCart;

        if (userCart != null) {
            finalCart = userCart;
            if (guestCart != null && !guestCart.getShoppingCartId().equals(finalCart.getShoppingCartId())) {
                performMerge(finalCart, guestCart);
            }
        } else if (guestCart != null) {
            finalCart = guestCart;
            if (userId != null) {
                User u = new User();
                u.setUserId(userId);
                finalCart.setUser(u);
                cartDAO.update(finalCart);
            }
        } else {
            ShoppingCart newCart = new ShoppingCart();
            if (userId != null) {
                User u = new User();
                u.setUserId(userId);
                newCart.setUser(u);
            } else {
                newCart.setSessionId(sessionId);
            }
            finalCart = cartDAO.save(newCart);
        }

        List<CartItem> items = cartItemDAO.findAllByShoppingCartId(finalCart.getShoppingCartId());
        finalCart.setCartItems(new HashSet<>(items));
        return finalCart;
    }

    private void performMerge(ShoppingCart targetUserCart, ShoppingCart sourceGuestCart) {
        List<CartItem> guestItems = cartItemDAO.findAllByShoppingCartId(sourceGuestCart.getShoppingCartId());
        List<CartItem> userItems = cartItemDAO.findAllByShoppingCartId(targetUserCart.getShoppingCartId());

        for (CartItem guestItem : guestItems) {
            Optional<CartItem> existingItem = userItems.stream().filter(ui -> ui.getProduct().getProductId().equals(guestItem.getProduct().getProductId())).findFirst();

            if (existingItem.isPresent()) {
                CartItem ui = existingItem.get();
                int combinedQuantity = ui.getQuantity() + guestItem.getQuantity();

                Optional<Product> prodOpt = productDAO.findById(ui.getProduct().getProductId());
                if (prodOpt.isPresent()) {
                    int maxStock = prodOpt.get().getQuantity();
                    if (combinedQuantity > maxStock) {
                        combinedQuantity = maxStock;
                    }
                }

                ui.setQuantity(combinedQuantity);
                cartItemDAO.update(ui);
                cartItemDAO.delete(guestItem.getCartItemId());
            } else {
                guestItem.setShoppingCart(targetUserCart);
                cartItemDAO.update(guestItem);
            }
        }
        cartDAO.delete(sourceGuestCart.getShoppingCartId());
    }

    @Override
    public void addItemToCart(Integer userId, String sessionId, int productId, int quantity) {
        ShoppingCart cart = getCart(userId, sessionId);
        Optional<Product> productOpt = productDAO.findById(productId);

        if (productOpt.isEmpty()) {
            throw new RuntimeException("Product not found.");
        }
        Product product = productOpt.get();

        if (product.getQuantity() < quantity) {
            throw new RuntimeException("Not enough inventory available.");
        }

        Optional<CartItem> existingItem = cart.getCartItems().stream().filter(item -> item.getProduct().getProductId().equals(productId)).findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;

            if (product.getQuantity() < newQuantity) {
                throw new RuntimeException("Not enough inventory for total quantity.");
            }

            item.setQuantity(newQuantity);
            cartItemDAO.update(item);
        } else {
            CartItem newItem = new CartItem(quantity, cart, product);
            cartItemDAO.save(newItem);
        }
    }

    @Override
    public int getCartItemCount(Integer userId, String sessionId) {
        ShoppingCart cart = getCart(userId, sessionId);
        if (cart.getShoppingCartId() == null) return 0;
        List<CartItem> items = cartItemDAO.findAllByShoppingCartId(cart.getShoppingCartId());
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    @Override
    public void removeFromCart(Integer userId, String sessionId, int productId) {
        ShoppingCart cart = getCart(userId, sessionId);
        if (cart.getCartItems() == null) return;
        Optional<CartItem> itemToRemove = cart.getCartItems().stream().filter(item -> item.getProduct().getProductId().equals(productId)).findFirst();

        if (itemToRemove.isPresent()) {
            cartItemDAO.delete(itemToRemove.get().getCartItemId());
        }
    }

    @Override
    public void clearCart(Integer userId, String sessionId) {
        ShoppingCart cart = getCart(userId, sessionId);
        if (cart.getCartItems() == null) return;
        for (CartItem item : cart.getCartItems()) {
            cartItemDAO.delete(item.getCartItemId());
        }
    }

    @Override
    public void updateItemQuantity(Integer userId, String sessionId, int productId, int quantity) {
        ShoppingCart cart = getCart(userId, sessionId);
        if (cart.getCartItems() == null) return;

        Optional<CartItem> existingItem = cart.getCartItems().stream().filter(item -> item.getProduct().getProductId().equals(productId)).findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            Optional<Product> productOpt = productDAO.findById(productId);

            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                if (product.getQuantity() < quantity) {
                    throw new RuntimeException("Not enough stock available. Max: " + product.getQuantity());
                }
                item.setQuantity(quantity);
                cartItemDAO.update(item);
            }
        }
    }

    @Override
    public void mergeCarts(Integer userId, String sessionId) {
        List<ShoppingCart> allCarts = cartDAO.findAll();

        Optional<ShoppingCart> guestCartOpt = allCarts.stream().filter(c -> sessionId.equals(c.getSessionId()) && (c.getUser() == null || c.getUser().getUserId() == 0)).findFirst();

        if (guestCartOpt.isEmpty()) return;

        ShoppingCart guestCart = guestCartOpt.get();
        List<CartItem> guestItems = cartItemDAO.findAllByShoppingCartId(guestCart.getShoppingCartId());

        if (guestItems.isEmpty()) return;

        Optional<ShoppingCart> userCartOpt = allCarts.stream().filter(c -> c.getUser() != null && c.getUser().getUserId().equals(userId)).findFirst();

        if (userCartOpt.isEmpty()) {
            User user = new User();
            user.setUserId(userId);
            guestCart.setUser(user);
            guestCart.setSessionId(null);
            cartDAO.update(guestCart);
        } else {
            ShoppingCart userCart = userCartOpt.get();
            List<CartItem> userItems = cartItemDAO.findAllByShoppingCartId(userCart.getShoppingCartId());

            for (CartItem guestItem : guestItems) {
                Optional<CartItem> existingUserItem = userItems.stream().filter(ui -> ui.getProduct().getProductId().equals(guestItem.getProduct().getProductId())).findFirst();

                if (existingUserItem.isPresent()) {
                    CartItem ui = existingUserItem.get();
                    int desiredQuantity = ui.getQuantity() + guestItem.getQuantity();

                    int availableStock = 0;
                    Optional<Product> prodOpt = productDAO.findById(ui.getProduct().getProductId());
                    if (prodOpt.isPresent()) {
                        availableStock = prodOpt.get().getQuantity();
                    }

                    if (desiredQuantity > availableStock) {
                        ui.setQuantity(availableStock);
                    } else {
                        ui.setQuantity(desiredQuantity);
                    }
                    cartItemDAO.update(ui);
                    cartItemDAO.delete(guestItem.getCartItemId());
                } else {
                    guestItem.setShoppingCart(userCart);
                    cartItemDAO.update(guestItem);
                }
            }
            cartDAO.delete(guestCart.getShoppingCartId());
        }
    }

    @Override
    public List<String> validateCartInventory(ShoppingCart cart, String sessionId) {
        List<String> removedItems = new ArrayList<>();

        if (cart == null || cart.getCartItems() == null) {
            return removedItems;
        }

        List<CartItem> itemsList = new ArrayList<>(cart.getCartItems());

        for (int i = itemsList.size() - 1; i >= 0; i--) {
            CartItem item = itemsList.get(i);

            Optional<Product> currentProductOpt = productDAO.findById(item.getProduct().getProductId());

            if (currentProductOpt.isEmpty()) {
                this.removeFromCart(cart.getUser() != null ? cart.getUser().getUserId() : null, cart.getSessionId(), item.getProduct().getProductId());
                removedItems.add(item.getProduct().getName() + " (Product not found)");
                itemsList.remove(i);
                continue;
            }

            Product productInDB = currentProductOpt.get();
            int requiredQuantity = item.getQuantity();
            int availableQuantity = productInDB.getQuantity();

            if (requiredQuantity > availableQuantity) {
                if (availableQuantity == 0) {
                    this.removeFromCart(cart.getUser() != null ? cart.getUser().getUserId() : null, cart.getSessionId(), item.getProduct().getProductId());
                    removedItems.add(item.getProduct().getName() + " (Out of stock)");
                    itemsList.remove(i);
                } else {
                    this.updateItemQuantity(cart.getUser() != null ? cart.getUser().getUserId() : null, cart.getSessionId(), item.getProduct().getProductId(), availableQuantity);
                    removedItems.add(item.getProduct().getName() + " (Quantity reduced to " + availableQuantity + " due to stock limits)");

                    item.setQuantity(availableQuantity);
                }
            }
        }

        cart.setCartItems(new HashSet<>(itemsList));

        return removedItems;
    }
}