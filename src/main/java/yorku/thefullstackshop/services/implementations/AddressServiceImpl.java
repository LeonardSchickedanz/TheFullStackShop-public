package yorku.thefullstackshop.services.implementations;

import yorku.thefullstackshop.daos.implementations.AddressDAOImpl;
import yorku.thefullstackshop.daos.interfaces.AddressDAO;
import yorku.thefullstackshop.models.Address;
import yorku.thefullstackshop.services.interfaces.AddressService;

import java.util.Optional;

public class AddressServiceImpl implements AddressService {

    private final AddressDAO addressDAO;

    public AddressServiceImpl() {
        this.addressDAO = new AddressDAOImpl();
    }

    public AddressServiceImpl(AddressDAO addressDAO) {
        this.addressDAO = addressDAO;
    }

    @Override
    public Optional<Address> findLatestAddressByUserId(int userId) {
        return addressDAO.findLatestAddressByUserId(userId);
    }

    @Override
    public Address saveAddress(Address address) {
        return addressDAO.save(address);
    }

    @Override
    public Address updateAddress(Address address) {
        return addressDAO.update(address);
    }

}