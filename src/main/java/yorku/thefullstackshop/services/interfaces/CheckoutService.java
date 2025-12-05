package yorku.thefullstackshop.services.interfaces;

import yorku.thefullstackshop.models.Address;
import yorku.thefullstackshop.models.Order;
import yorku.thefullstackshop.models.PaymentInfo;
import yorku.thefullstackshop.models.User;

public interface CheckoutService {
    Order processCheckout(User user, Address address, PaymentInfo paymentInfo, String cvc) throws Exception;
}