package yorku.thefullstackshop.services.implementations;

import yorku.thefullstackshop.daos.implementations.PaymentInfoDAOImpl;
import yorku.thefullstackshop.daos.interfaces.PaymentInfoDAO;
import yorku.thefullstackshop.models.PaymentInfo;
import yorku.thefullstackshop.services.interfaces.PaymentService;

import java.util.Optional;
import java.util.Random;

public class PaymentServiceImpl implements PaymentService {

    private final PaymentInfoDAO paymentInfoDAO;
    private final Random random;

    private static final String DATE_PATTERN = "^(0[1-9]|1[0-2])/\\d{2}$";
    private static final String CVC_PATTERN = "^\\d{3,4}$";
    private static final String CARD_PATTERN = "^\\d{13,19}$";

    public PaymentServiceImpl(PaymentInfoDAO paymentInfoDAO, Random random) {
        this.paymentInfoDAO = paymentInfoDAO;
        this.random = random;
    }

    public PaymentServiceImpl() {
        this.paymentInfoDAO = new PaymentInfoDAOImpl();
        this.random = new Random();
    }

    @Override
    public boolean processPayment(String cardNumber, String expiryDate, String cvc, double amount) {
        if (cardNumber == null || !cardNumber.matches(CARD_PATTERN)) {
            System.out.println("Invalid Card Number format.");
            return false;
        }

        if (expiryDate == null || !expiryDate.matches(DATE_PATTERN)) {
            System.out.println("Invalid Expiry Date format (Expected MM/YY).");
            return false;
        }

        if (cvc == null || !cvc.matches(CVC_PATTERN)) {
            System.out.println("Invalid CVC format.");
            return false;
        }

        if (random.nextInt(5) == 0) {
            System.out.println("Payment rejected by simulation (Random 1/5).");
            return false;
        }
        return true;
    }

    @Override
    public Optional<PaymentInfo> findLatestPaymentInfoByUserId(int userId) {
        return paymentInfoDAO.findLatestPaymentInfoByUserId(userId);
    }

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoDAO.save(paymentInfo);
    }
}