package yorku.thefullstackshop.daos.interfaces;

import yorku.thefullstackshop.models.Address;

import java.util.Optional;

public interface AddressDAO extends BaseDAO<Address> {
    Optional<Address> findLatestAddressByUserId(int userId);
}
