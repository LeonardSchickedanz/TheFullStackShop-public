package yorku.thefullstackshop.services.interfaces;

import yorku.thefullstackshop.models.PaymentInfo;

import java.util.Optional;

public interface PaymentService {
    boolean processPayment(String cardNumber, String expiryDate, String cvc, double amount);

    Optional<PaymentInfo> findLatestPaymentInfoByUserId(int userId);

    public void savePaymentInfo(PaymentInfo paymentInfo);
}