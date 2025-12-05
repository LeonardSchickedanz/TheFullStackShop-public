package yorku.thefullstackshop.daos.interfaces;

import yorku.thefullstackshop.models.PaymentInfo;

import java.util.Optional;

public interface PaymentInfoDAO extends BaseDAO<PaymentInfo> {
    Optional<PaymentInfo> findLatestPaymentInfoByUserId(int userId);
}
