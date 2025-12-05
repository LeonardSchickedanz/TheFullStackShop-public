package yorku.thefullstackshop.servicetests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yorku.thefullstackshop.daos.interfaces.AddressDAO;
import yorku.thefullstackshop.models.Address;
import yorku.thefullstackshop.services.implementations.AddressServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressDAO mockAddressDAO;

    @InjectMocks
    private AddressServiceImpl addressService;

    private final int TEST_USER_ID = 5;

    private Address createTestAddress() {
        Address address = new Address();
        address.setAddressId(10);
        address.setStreet("Mock Street");
        return address;
    }


    @Test
    void testFindLatestAddressByUserId_Success() {
        Address expectedAddress = createTestAddress();

        when(mockAddressDAO.findLatestAddressByUserId(TEST_USER_ID)).thenReturn(Optional.of(expectedAddress));

        Optional<Address> result = addressService.findLatestAddressByUserId(TEST_USER_ID);

        assertTrue(result.isPresent());
        assertEquals(expectedAddress.getStreet(), result.get().getStreet());
        verify(mockAddressDAO, times(1)).findLatestAddressByUserId(TEST_USER_ID);
    }

    @Test
    void testFindLatestAddressByUserId_NotFound() {
        when(mockAddressDAO.findLatestAddressByUserId(TEST_USER_ID)).thenReturn(Optional.empty());

        Optional<Address> result = addressService.findLatestAddressByUserId(TEST_USER_ID);

        assertFalse(result.isPresent());
        verify(mockAddressDAO).findLatestAddressByUserId(TEST_USER_ID);
    }


    @Test
    void testSaveAddress_CallsDaoSave() {
        Address newAddress = new Address();

        when(mockAddressDAO.save(any(Address.class))).thenReturn(createTestAddress());

        Address result = addressService.saveAddress(newAddress);

        assertNotNull(result.getStreet());
        verify(mockAddressDAO, times(1)).save(newAddress);
    }

    @Test
    void testUpdateAddress_CallsDaoUpdate() {
        Address existingAddress = createTestAddress();
        existingAddress.setCity("Updated City");

        when(mockAddressDAO.update(existingAddress)).thenReturn(existingAddress);

        Address result = addressService.updateAddress(existingAddress);

        assertEquals("Updated City", result.getCity());
        verify(mockAddressDAO, times(1)).update(existingAddress);
    }
}