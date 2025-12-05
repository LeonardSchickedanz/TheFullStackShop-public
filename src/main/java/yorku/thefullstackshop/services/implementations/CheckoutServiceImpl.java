package yorku.thefullstackshop.services.implementations;

import yorku.thefullstackshop.daos.implementations.AddressDAOImpl;
import yorku.thefullstackshop.daos.implementations.PaymentInfoDAOImpl;
import yorku.thefullstackshop.daos.interfaces.AddressDAO;
import yorku.thefullstackshop.daos.interfaces.PaymentInfoDAO;
import yorku.thefullstackshop.models.*;
import yorku.thefullstackshop.services.interfaces.CheckoutService;
import yorku.thefullstackshop.services.interfaces.OrderService;
import yorku.thefullstackshop.services.interfaces.PaymentService;
import yorku.thefullstackshop.services.interfaces.ShoppingCartService;

public class CheckoutServiceImpl implements CheckoutService {

    private final AddressDAO addressDAO = new AddressDAOImpl();
    private final PaymentInfoDAO paymentInfoDAO = new PaymentInfoDAOImpl();
    private final PaymentService paymentService = new PaymentServiceImpl();
    private final ShoppingCartService cartService = new ShoppingCartServiceImpl();
    private final OrderService orderService = new OrderServiceImpl();

    @Override
    public Order processCheckout(User user, Address address, PaymentInfo paymentInfo, String cvc) throws Exception {
        return processCheckout(user, address, paymentInfo, cvc, true, true);
    }

    public Order processCheckout(User user, Address address, PaymentInfo paymentInfo, String cvc,
                                 boolean saveAddress, boolean savePaymentInfo) throws Exception {

        boolean paymentSuccess = paymentService.processPayment(
                paymentInfo.getCardNumber(),
                paymentInfo.getExpiryDate(),
                cvc,
                0.0
        );

        if (!paymentSuccess) {
            throw new RuntimeException("Credit Card Authorization Failed.");
        }

        Address savedAddress;
        if (saveAddress) {
            address.setUser(user);
            savedAddress = addressDAO.save(address);
        } else {
            address.setUser(user);
            savedAddress = addressDAO.save(address);
        }

        if (savePaymentInfo) {
            paymentInfo.setUser(user);
            paymentInfoDAO.save(paymentInfo);
        }

        ShoppingCart cart = cartService.getCart(user.getUserId(), null);

        Order order = orderService.placeOrderWithAddress(user, cart, paymentInfo.getCardNumber(), savedAddress);

        cartService.clearCart(user.getUserId(), null);

        return order;
    }
}