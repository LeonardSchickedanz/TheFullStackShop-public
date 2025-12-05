package yorku.thefullstackshop.services.implementations;

import yorku.thefullstackshop.daos.implementations.AddressDAOImpl;
import yorku.thefullstackshop.daos.implementations.OrderDAOImpl;
import yorku.thefullstackshop.daos.implementations.ProductDAOImpl;
import yorku.thefullstackshop.daos.implementations.UserDAOImpl;
import yorku.thefullstackshop.daos.interfaces.AddressDAO;
import yorku.thefullstackshop.daos.interfaces.OrderDAO;
import yorku.thefullstackshop.daos.interfaces.ProductDAO;
import yorku.thefullstackshop.daos.interfaces.UserDAO;
import yorku.thefullstackshop.models.*;
import yorku.thefullstackshop.services.interfaces.AdminService;
import yorku.thefullstackshop.services.interfaces.PaymentService;

import java.util.List;
import java.util.Optional;

public class AdminServiceImpl implements AdminService {

    private final ProductDAO productDAO;
    private final UserDAO userDAO;
    private final OrderDAO orderDAO;
    private final AddressDAO addressDAO;
    private final PaymentService paymentService;

    public AdminServiceImpl() {
        this.productDAO = new ProductDAOImpl();
        this.userDAO = new UserDAOImpl();
        this.orderDAO = new OrderDAOImpl();
        this.addressDAO = new AddressDAOImpl();
        this.paymentService = new PaymentServiceImpl();
    }

    public AdminServiceImpl(ProductDAO productDAO, UserDAO userDAO, OrderDAO orderDAO, AddressDAO addressDAO, PaymentService paymentService) {
        this.productDAO = productDAO;
        this.userDAO = userDAO;
        this.orderDAO = orderDAO;
        this.addressDAO = addressDAO;
        this.paymentService = paymentService;
    }

    public List<Product> getAllProducts() {
        return productDAO.findAll();
    }

    @Override
    public int updateInventory(int productId, int newQuantity) {
        int updatedQuantity = 0;
        Optional<Product> pOpt = productDAO.findById(productId);
        if (pOpt.isPresent()) {
            if (productDAO instanceof ProductDAOImpl) {
                updatedQuantity = productDAO.updateQuantity(productId, newQuantity);
            } else {
                Product p = pOpt.get();
                p.setQuantity(newQuantity);
                updatedQuantity = newQuantity;
            }
        }
        return updatedQuantity;
    }

    @Override
    public List<Order> getSalesHistory(String email, String product, String dateFrom, String dateTo) {
        return orderDAO.findSalesHistory(email, product, dateFrom, dateTo);
    }

    public Optional<Order> getOrderById(int id) {
        return orderDAO.findById(id);
    }

    @Override
    public List<User> getAllCustomers() {
        return userDAO.findAll();
    }

    @Override
    public Optional<User> getCustomerById(int id) {
        return userDAO.findById(id);
    }

    public Optional<Address> getCustomerAddress(int userId) {
        return addressDAO.findLatestAddressByUserId(userId);
    }

    public Optional<PaymentInfo> getCustomerPayment(int userId) {
        return paymentService.findLatestPaymentInfoByUserId(userId);
    }

    @Override
    public User updateCustomer(User user) {
        return userDAO.update(user);
    }

    public User updateCustomerProfile(int userId, String fName, String lName, String email, Integer addressId, String street, String streetNum, String zip, String city, String prov, String country, Integer paymentId, String cardNum, String expiry, String cvc) {

        Optional<User> userOpt = userDAO.findById(userId);
        User updatedUser = null;

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFirstName(fName);
            user.setLastName(lName);
            user.setEmail(email);
            updatedUser = userDAO.update(user);
        } else {
            return null;
        }

        Address address = new Address();
        if (addressId != null) address.setAddressId(addressId);
        address.setStreet(street);
        address.setStreetNumber(streetNum);
        address.setPostalCode(zip);
        address.setCity(city);
        address.setProvince(prov);
        address.setCountry(country);

        User userRef = new User();
        userRef.setUserId(userId);
        address.setUser(userRef);

        if (address.getAddressId() != null && address.getAddressId() > 0) {
            addressDAO.update(address);
        } else {
            if (street != null && !street.isEmpty()) {
                addressDAO.save(address);
            }
        }

        if (cardNum != null && !cardNum.isEmpty()) {
            PaymentInfo payment = new PaymentInfo();
            if (paymentId != null) payment.setPaymentInfoId(paymentId);
            payment.setCardNumber(cardNum);
            payment.setExpiryDate(expiry);
            payment.setCvc(cvc);
            payment.setUser(userRef);

            paymentService.savePaymentInfo(payment);
        }

        return updatedUser;
    }

    @Override
    public List<User> getAllCustomers(String searchEmail) {
        List<User> allUsers = userDAO.findAll();

        if (searchEmail == null || searchEmail.trim().isEmpty()) {
            return allUsers;
        }

        return allUsers.stream().filter(u -> u.getEmail().toLowerCase().contains(searchEmail.toLowerCase())).collect(java.util.stream.Collectors.toList());
    }
}