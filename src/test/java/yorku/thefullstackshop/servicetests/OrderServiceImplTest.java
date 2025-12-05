package yorku.thefullstackshop.servicetests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yorku.thefullstackshop.daos.implementations.CartItemDAOImpl;
import yorku.thefullstackshop.daos.implementations.OrderDAOImpl;
import yorku.thefullstackshop.daos.implementations.OrderLineDAOImpl;
import yorku.thefullstackshop.daos.implementations.ProductDAOImpl;
import yorku.thefullstackshop.models.*;
import yorku.thefullstackshop.services.implementations.OrderServiceImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderDAOImpl orderDAO;
    @Mock
    private OrderLineDAOImpl orderLineDAO;
    @Mock
    private ProductDAOImpl productDAO;
    @Mock
    private CartItemDAOImpl cartItemDAO;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User setupUser() {
        User user = new User();
        user.setUserId(1);
        return user;
    }

    private Product setupProduct(int id, BigDecimal price, int stock) {
        Product p = new Product();
        p.setProductId(id);
        p.setName("Product " + id);
        p.setPrice(price);
        p.setQuantity(stock);
        return p;
    }

    private CartItem setupCartItem(int cartItemId, Product product, int quantity) {
        CartItem item = new CartItem();
        item.setCartItemId(cartItemId);
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }

    private ShoppingCart setupCart(List<CartItem> items) {
        ShoppingCart cart = new ShoppingCart();
        cart.setCartItems(new HashSet<>(items));
        return cart;
    }

    @Test
    void testGetOrdersByUserId() {
        List<Order> expectedOrders = Arrays.asList(new Order(), new Order());
        when(orderDAO.findAllByUserId(1)).thenReturn(expectedOrders);

        List<Order> result = orderService.getOrdersByUserId(1);

        assertEquals(2, result.size());
        verify(orderDAO).findAllByUserId(1);
    }

    @Test
    void testGetOrderById_FoundAndLinesAttached() {
        Order mockOrder = new Order();
        mockOrder.setOrderId(100);
        List<OrderLine> mockLines = Arrays.asList(new OrderLine(), new OrderLine());

        when(orderDAO.findById(100)).thenReturn(Optional.of(mockOrder));
        when(orderLineDAO.findAllByOrderId(100)).thenReturn(mockLines);

        Optional<Order> result = orderService.getOrderById(100);

        assertTrue(result.isPresent());
        assertEquals(2, result.get().getOrderLines().size());
        verify(orderLineDAO).findAllByOrderId(100);
    }

    @Test
    void testGetOrderById_NotFound() {
        when(orderDAO.findById(999)).thenReturn(Optional.empty());

        Optional<Order> result = orderService.getOrderById(999);

        assertFalse(result.isPresent());
        verify(orderLineDAO, never()).findAllByOrderId(anyInt());
    }
}