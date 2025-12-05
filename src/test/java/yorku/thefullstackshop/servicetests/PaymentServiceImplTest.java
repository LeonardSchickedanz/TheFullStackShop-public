package yorku.thefullstackshop.servicetests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yorku.thefullstackshop.services.implementations.PaymentServiceImpl;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private Random mockRandom;

    private PaymentServiceImpl paymentService;

    private final String VALID_CARD = "1234567890123456";
    private final String VALID_EXPIRY = "12/26";
    private final String VALID_CVC = "123";
    private final double VALID_AMOUNT = 100.00;

    @BeforeEach
    void setUp() {
        this.paymentService = new PaymentServiceImpl(null, mockRandom);
    }

    @Test
    void testProcessPayment_Success_ApprovedByRandom() {
        when(mockRandom.nextInt(5)).thenReturn(1);

        boolean result = paymentService.processPayment(VALID_CARD, VALID_EXPIRY, VALID_CVC, VALID_AMOUNT);

        assertTrue(result);
        verify(mockRandom, times(1)).nextInt(5);
    }

    @Test
    void testProcessPayment_Rejected_ByRandom() {
        when(mockRandom.nextInt(5)).thenReturn(0);

        boolean result = paymentService.processPayment(VALID_CARD, VALID_EXPIRY, VALID_CVC, VALID_AMOUNT);

        assertFalse(result);
        verify(mockRandom, times(1)).nextInt(5);
    }

    @Test
    void testProcessPayment_InvalidCardNumber_Length() {
        boolean result = paymentService.processPayment("12345", VALID_EXPIRY, VALID_CVC, VALID_AMOUNT);
        assertFalse(result);
        verify(mockRandom, never()).nextInt(anyInt());
    }

    @Test
    void testProcessPayment_InvalidCardNumber_Null() {
        boolean result = paymentService.processPayment(null, VALID_EXPIRY, VALID_CVC, VALID_AMOUNT);
        assertFalse(result);
    }

    @Test
    void testProcessPayment_InvalidExpiryDate_Format() {
        boolean result = paymentService.processPayment(VALID_CARD, "12-26", VALID_CVC, VALID_AMOUNT);
        assertFalse(result);
        verify(mockRandom, never()).nextInt(anyInt());
    }

    @Test
    void testProcessPayment_InvalidCVC_Format() {
        boolean result = paymentService.processPayment(VALID_CARD, VALID_EXPIRY, "12345", VALID_AMOUNT);
        assertFalse(result);
        verify(mockRandom, never()).nextInt(anyInt());
    }
}