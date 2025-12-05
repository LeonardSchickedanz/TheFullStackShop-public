package yorku.thefullstackshop.models;

import jakarta.persistence.*;

@Entity
@Table(name = "PaymentInfo")
public class PaymentInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_info_id")
    private Integer paymentInfoId;

    @Column(name = "card_number", nullable = false, length = 255)
    private String cardNumber;

    @Column(name = "expiry_date", nullable = false, length = 10)
    private String expiryDate;

    @Column(name = "cvc", nullable = false, length = 3)
    private String cvc;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public PaymentInfo() {}

    public PaymentInfo(String cardNumber, String expiryDate, String cvc, User user) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvc = cvc;
        this.user = user;
    }

    public Integer getPaymentInfoId() {
        return paymentInfoId;
    }

    public void setPaymentInfoId(Integer paymentInfoId) {
        this.paymentInfoId = paymentInfoId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCvc() {
        return cvc;
    }

    public void setCvc(String cvc) {
        this.cvc = cvc;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
