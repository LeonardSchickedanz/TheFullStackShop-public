package yorku.thefullstackshop.servicetests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yorku.thefullstackshop.daos.implementations.CartItemDAOImpl;
import yorku.thefullstackshop.daos.implementations.ProductDAOImpl;
import yorku.thefullstackshop.daos.implementations.ShoppingCartDAOImpl;
import yorku.thefullstackshop.models.CartItem;
import yorku.thefullstackshop.models.Product;
import yorku.thefullstackshop.models.ShoppingCart;
import yorku.thefullstackshop.models.User;
import yorku.thefullstackshop.services.implementations.ShoppingCartServiceImpl;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceImplTest {

    @Mock
    private ShoppingCartDAOImpl cartDAO;
    @Mock
    private CartItemDAOImpl cartItemDAO;
    @Mock
    private ProductDAOImpl productDAO;

    @InjectMocks
    private ShoppingCartServiceImpl shoppingCartService;

    private ShoppingCart setupCart(Integer cartId, Integer userId, String sessionId) {
        ShoppingCart cart = new ShoppingCart();
        cart.setShoppingCartId(cartId);
        if (userId != null) {
            User u = new User();
            u.setUserId(userId);
            cart.setUser(u);
        } else {
            cart.setSessionId(sessionId);
        }
        return cart;
    }

    private Product setupProduct(int id, int stock) {
        Product p = new Product();
        p.setProductId(id);
        p.setPrice(BigDecimal.TEN);
        p.setQuantity(stock);
        return p;
    }

    private CartItem setupCartItem(int cartItemId, Product product, int quantity, ShoppingCart cart) {
        CartItem item = new CartItem();
        item.setCartItemId(cartItemId);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setShoppingCart(cart);
        return item;
    }

    @Test
    void testGetCart_ExistingUserCartFound() {
        ShoppingCart existingCart = setupCart(100, 5, null);
        List<ShoppingCart> allCarts = Collections.singletonList(existingCart);
        List<CartItem> items = Arrays.asList(new CartItem(), new CartItem());

        when(cartDAO.findAll()).thenReturn(allCarts);
        when(cartItemDAO.findAllByShoppingCartId(100)).thenReturn(items);

        ShoppingCart result = shoppingCartService.getCart(5, "sessionXYZ");

        assertEquals(100, result.getShoppingCartId());
        assertEquals(2, result.getCartItems().size());
        verify(cartDAO).findAll();
    }

    @Test
    void testGetCart_NewUserCartCreated() {
        when(cartDAO.findAll()).thenReturn(Collections.emptyList());
        ShoppingCart savedCart = setupCart(200, 6, null);

        when(cartDAO.save(any(ShoppingCart.class))).thenReturn(savedCart);

        ShoppingCart result = shoppingCartService.getCart(6, null);

        assertEquals(200, result.getShoppingCartId());
        assertNotNull(result.getUser());
        verify(cartDAO).save(any(ShoppingCart.class));
    }


    @Test
    void testAddItemToCart_NewItemAdded() {
        int productId = 10;
        int quantity = 3;
        Product product = setupProduct(productId, 10);
        ShoppingCart cart = setupCart(10, null, "sess");
        cart.setCartItems(new HashSet<>());

        when(productDAO.findById(productId)).thenReturn(Optional.of(product));
        when(cartDAO.findAll()).thenReturn(Collections.singletonList(cart));
        when(cartItemDAO.findAllByShoppingCartId(10)).thenReturn(Collections.emptyList());

        shoppingCartService.addItemToCart(null, "sess", productId, quantity);

        verify(cartItemDAO).save(any(CartItem.class));
        verify(cartItemDAO, never()).update(any(CartItem.class));
    }

    @Test
    void testAddItemToCart_ExistingItemUpdated() {
        int productId = 10;
        int stock = 10;
        Product product = setupProduct(productId, stock);
        ShoppingCart cart = setupCart(10, 5, null);
        CartItem existingItem = setupCartItem(1, product, 3, cart);
        cart.setCartItems(new HashSet<>(Collections.singletonList(existingItem)));

        when(productDAO.findById(productId)).thenReturn(Optional.of(product));
        when(cartDAO.findAll()).thenReturn(Collections.singletonList(cart));
        when(cartItemDAO.findAllByShoppingCartId(10)).thenReturn(Collections.singletonList(existingItem));

        shoppingCartService.addItemToCart(5, null, productId, 2);

        assertEquals(5, existingItem.getQuantity());
        verify(cartItemDAO).update(existingItem);
        verify(cartItemDAO, never()).save(any(CartItem.class));
    }

    @Test
    void testAddItemToCart_ThrowsException_InitialStockCheck() {
        int productId = 10;
        Product product = setupProduct(productId, 5);
        when(productDAO.findById(productId)).thenReturn(Optional.of(product));
        when(cartDAO.findAll()).thenReturn(Collections.emptyList());
        when(cartDAO.save(any(ShoppingCart.class))).thenReturn(setupCart(1, null, "sess"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            shoppingCartService.addItemToCart(null, "sess", productId, 6);
        });

        assertEquals("Not enough inventory available.", exception.getMessage());
        verify(cartItemDAO, never()).save(any(CartItem.class));
    }


    @Test
    void testMergeCarts_GuestToNewUserCart() {
        int userId = 5;
        String sessionId = "guest_sess";
        Product p1 = setupProduct(10, 5);
        ShoppingCart guestCart = setupCart(100, null, sessionId);
        CartItem guestItem = setupCartItem(1, p1, 2, guestCart);

        when(cartDAO.findAll()).thenReturn(Collections.singletonList(guestCart));
        when(cartItemDAO.findAllByShoppingCartId(100)).thenReturn(Collections.singletonList(guestItem));

        shoppingCartService.mergeCarts(userId, sessionId);

        verify(cartDAO).update(guestCart);
        assertEquals(userId, guestCart.getUser().getUserId());
        assertNull(guestCart.getSessionId());

        verify(cartDAO, never()).delete(anyInt());
        verify(cartItemDAO, never()).update(any(CartItem.class));
    }

    @Test
    void testMergeCarts_MergeItems_WithStockLimit() {
        int userId = 5;
        String sessionId = "guest_sess";
        Product p1 = setupProduct(10, 5);

        ShoppingCart userCart = setupCart(200, userId, null);
        CartItem userItem = setupCartItem(2, p1, 3, userCart);

        ShoppingCart guestCart = setupCart(100, null, sessionId);
        CartItem guestItem = setupCartItem(1, p1, 4, guestCart);

        when(cartDAO.findAll()).thenReturn(Arrays.asList(userCart, guestCart));
        when(cartItemDAO.findAllByShoppingCartId(100)).thenReturn(Collections.singletonList(guestItem));
        when(cartItemDAO.findAllByShoppingCartId(200)).thenReturn(Collections.singletonList(userItem));
        when(productDAO.findById(10)).thenReturn(Optional.of(p1));

        shoppingCartService.mergeCarts(userId, sessionId);

        assertEquals(5, userItem.getQuantity());
        verify(cartItemDAO).update(userItem);
        verify(cartItemDAO).delete(guestItem.getCartItemId());
        verify(cartDAO).delete(100);
    }
}