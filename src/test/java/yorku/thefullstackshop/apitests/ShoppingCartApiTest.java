package yorku.thefullstackshop.apitests;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yorku.thefullstackshop.apis.ShoppingCartApi;
import yorku.thefullstackshop.models.User;
import yorku.thefullstackshop.services.interfaces.ShoppingCartService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingCartApiTest {

    @Mock
    private ShoppingCartService mockShoppingCartService;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpSession mockSession;

    @InjectMocks
    private ShoppingCartApi shoppingCartApi;

    private final int TEST_PRODUCT_ID = 42;
    private final int TEST_USER_ID = 10;
    private final String TEST_SESSION_ID = "XYZ123";

    private void setupMockRequest(boolean loggedIn, int currentCartCount) {
        when(mockRequest.getSession(true)).thenReturn(mockSession);
        when(mockSession.getId()).thenReturn(TEST_SESSION_ID);

        User user = null;
        if (loggedIn) {
            user = new User();
            user.setUserId(TEST_USER_ID);
            when(mockSession.getAttribute("user")).thenReturn(user);
        } else {
            when(mockSession.getAttribute("user")).thenReturn(null);
        }

        lenient().when(mockShoppingCartService.getCartItemCount(any(), eq(TEST_SESSION_ID))).thenReturn(currentCartCount + 1);
    }

    @Test
    void testAddToCart_UserLoggedIn_Success() {
        final int quanity = 1;
        setupMockRequest(true, 5);

        Response response = shoppingCartApi.addToCart(TEST_PRODUCT_ID, quanity, mockRequest);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{\"count\": 6}", response.getEntity());

        verify(mockShoppingCartService).addItemToCart(eq(TEST_USER_ID), eq(TEST_SESSION_ID), eq(TEST_PRODUCT_ID), eq(1));
    }

    @Test
    void testAddToCart_GuestUser_Success() {
        final int quanity = 1;
        setupMockRequest(false, 1);

        Response response = shoppingCartApi.addToCart(TEST_PRODUCT_ID, quanity, mockRequest);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{\"count\": 2}", response.getEntity());

        verify(mockShoppingCartService).addItemToCart(isNull(), eq(TEST_SESSION_ID), eq(TEST_PRODUCT_ID), eq(1));
    }
}