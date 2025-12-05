package yorku.thefullstackshop.servicetests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yorku.thefullstackshop.daos.implementations.ProductDAOImpl;
import yorku.thefullstackshop.daos.interfaces.AddressDAO;
import yorku.thefullstackshop.daos.interfaces.OrderDAO;
import yorku.thefullstackshop.daos.interfaces.UserDAO;
import yorku.thefullstackshop.models.*;
import yorku.thefullstackshop.services.implementations.AdminServiceImpl;
import yorku.thefullstackshop.services.interfaces.PaymentService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private ProductDAOImpl productDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private OrderDAO orderDAO;

    @Mock
    private AddressDAO addressDAO;

    @Mock
    private PaymentService paymentService;

    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminServiceImpl(productDAO, userDAO, orderDAO, addressDAO, paymentService);
    }

    private User createMockUser(int id, String email, String fName, String lName, int roleId) {
        User user = new User();
        user.setUserId(id);
        user.setEmail(email);
        user.setFirstName(fName);
        user.setLastName(lName);

        Role role = new Role();
        role.setRoleId(roleId);
        user.setRole(role);
        return user;
    }

    @Test
    void testGetSalesHistory_Success() {
        String email = "admin@test.com";
        String product = "Laptop";
        List<Order> expectedOrders = Arrays.asList(new Order(), new Order());

        when(orderDAO.findSalesHistory(eq(email), eq(product), any(), any())).thenReturn(expectedOrders);

        List<Order> actualOrders = adminService.getSalesHistory(email, product, "2025-01-01", "2025-12-31");

        assertEquals(2, actualOrders.size());
        verify(orderDAO).findSalesHistory(eq(email), eq(product), any(), any());
    }

    @Test
    void testGetOrderById_Success() {
        Order order = new Order();
        order.setOrderId(10);
        when(orderDAO.findById(10)).thenReturn(Optional.of(order));

        Optional<Order> result = adminService.getOrderById(10);

        assertTrue(result.isPresent());
        verify(orderDAO).findById(10);
    }

    @Test
    void testGetAllCustomers_NoSearchTerm() {
        List<User> expectedUsers = Arrays.asList(createMockUser(1, "a@b.com", "A", "B", 1), createMockUser(2, "x@y.com", "X", "Y", 1));
        when(userDAO.findAll()).thenReturn(expectedUsers);

        List<User> result = adminService.getAllCustomers(null);

        assertEquals(2, result.size());
        verify(userDAO).findAll();
    }

    @Test
    void testGetAllCustomers_WithSearchTerm_FiltersCorrectly() {
        User user1 = createMockUser(1, "alice@test.com", "Alice", "S", 1);
        User user2 = createMockUser(2, "bob@example.com", "Bob", "J", 1);
        User user3 = createMockUser(3, "TESTER@yahoo.com", "Tester", "T", 1);

        List<User> allUsers = Arrays.asList(user1, user2, user3);
        when(userDAO.findAll()).thenReturn(allUsers);

        List<User> result = adminService.getAllCustomers("test");

        assertEquals(2, result.size());
        verify(userDAO).findAll();
    }

    @Test
    void testGetCustomerById_Found() {
        User user = createMockUser(1, "a@b.com", "A", "B", 1);
        when(userDAO.findById(1)).thenReturn(Optional.of(user));

        Optional<User> result = adminService.getCustomerById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getUserId());
    }

    @Test
    void testUpdateCustomer_Success() {
        User user = createMockUser(5, "u@u.com", "U", "U", 1);
        when(userDAO.update(any(User.class))).thenReturn(user);

        adminService.updateCustomer(user);

        verify(userDAO).update(user);
    }

    @Test
    void testUpdateInventory_ProductNotFound() {
        int productId = 20;
        int newQuantity = 5;

        when(productDAO.findById(productId)).thenReturn(Optional.empty());

        adminService.updateInventory(productId, newQuantity);

        verify(productDAO).findById(productId);
        verify(productDAO, never()).updateQuantity(anyInt(), anyInt());
    }

    @Test
    void testUpdateCustomerProfile_ExistingAddressAndPayment() {
        int userId = 5;
        User existingUser = createMockUser(userId, "old@mail.com", "Old", "User", 1);

        when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDAO.update(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User resultUser = adminService.updateCustomerProfile(userId, "New", "Name", "new@mail.com", 100, "Street", "1", "12345", "City", "Prov", "Country", 200, "1111", "12/25", "123");

        assertNotNull(resultUser);
        assertEquals("New", resultUser.getFirstName());

        verify(userDAO).update(argThat(u -> u.getFirstName().equals("New")));
        verify(addressDAO).update(any(Address.class));
        verify(paymentService).savePaymentInfo(any(PaymentInfo.class));
    }

    @Test
    void testUpdateCustomerProfile_NewAddressAndPayment() {
        int userId = 6;
        User existingUser = createMockUser(userId, "temp@mail.com", "Temp", "User", 1);

        when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDAO.update(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User resultUser = adminService.updateCustomerProfile(userId, "Temp", "User", "temp@mail.com", null, "NewStreet", "2", "67890", "Town", "Region", "UK", null, "9999", "01/30", "456");

        assertNotNull(resultUser);

        verify(addressDAO).save(any(Address.class));
        verify(paymentService).savePaymentInfo(any(PaymentInfo.class));
    }
}