package yorku.thefullstackshop.services.interfaces;

import yorku.thefullstackshop.models.Address;

import java.util.Optional;

public interface AddressService {

    Optional<Address> findLatestAddressByUserId(int userId);

    Address saveAddress(Address address);

    Address updateAddress(Address address);
}