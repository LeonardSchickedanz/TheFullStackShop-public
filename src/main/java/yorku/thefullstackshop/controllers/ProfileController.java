package yorku.thefullstackshop.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import yorku.thefullstackshop.models.Address;
import yorku.thefullstackshop.models.Order;
import yorku.thefullstackshop.models.PaymentInfo;
import yorku.thefullstackshop.models.User;
import yorku.thefullstackshop.services.implementations.AddressServiceImpl;
import yorku.thefullstackshop.services.implementations.AdminServiceImpl;
import yorku.thefullstackshop.services.implementations.OrderServiceImpl;
import yorku.thefullstackshop.services.interfaces.AddressService;
import yorku.thefullstackshop.services.interfaces.OrderService;
import yorku.thefullstackshop.utils.ValidationUtil;

import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "ProfileController", urlPatterns = {"/profile", "/profile/edit", "/profile/order"})
public class ProfileController extends HttpServlet {

    private final AdminServiceImpl adminService;
    private final OrderService orderService;
    private final AddressService addressService;

    public ProfileController() {
        this.adminService = new AdminServiceImpl();
        this.orderService = new OrderServiceImpl();
        this.addressService = new AddressServiceImpl();
    }

    private boolean isUserLoggedIn(HttpSession session) {
        return session != null && session.getAttribute("user") != null;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (!isUserLoggedIn(session)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User currentUser = (User) session.getAttribute("user");
        String path = req.getServletPath();
        int userId = currentUser.getUserId();

        Optional<Address> addressOpt = adminService.getCustomerAddress(userId);
        Optional<PaymentInfo> paymentOpt = adminService.getCustomerPayment(userId);

        if ("/profile".equals(path)) {
            addressOpt.ifPresent(a -> req.setAttribute("latestAddress", a));
            paymentOpt.ifPresent(p -> req.setAttribute("customerPayment", p));
            req.setAttribute("orders", orderService.getOrdersByUserId(userId));
            req.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(req, resp);
            return;
        }

        if ("/profile/order".equals(path)) {
            handleOrderView(req, resp, userId);
            return;
        }

        if ("/profile/edit".equals(path)) {
            Optional<User> userOpt = adminService.getCustomerById(userId);
            userOpt.ifPresent(u -> req.setAttribute("customer", u));
            addressOpt.ifPresent(a -> req.setAttribute("customerAddress", a));
            paymentOpt.ifPresent(p -> req.setAttribute("customerPayment", p));

            req.setAttribute("formAction", "profile/edit");
            req.setAttribute("backUrl", "profile");

            req.getRequestDispatcher("/WEB-INF/views/profileform.jsp").forward(req, resp);
        }
    }

    private void handleOrderView(HttpServletRequest req, HttpServletResponse resp, int userId) throws ServletException, IOException {
        String orderIdStr = req.getParameter("id");
        if (orderIdStr != null) {
            try {
                int orderId = Integer.parseInt(orderIdStr);
                Optional<Order> orderOpt = orderService.getOrderById(orderId);

                if (orderOpt.isPresent() && orderOpt.get().getUser().getUserId().equals(userId)) {
                    Order order = orderOpt.get();
                    Optional<User> userOpt = adminService.getCustomerById(userId);

                    if (userOpt.isPresent()) {
                        User fullUser = userOpt.get();
                        order.setUser(fullUser);

                        req.setAttribute("orderUser", fullUser);
                        req.setAttribute("order", order);
                        req.setAttribute("isConfirmation", false);
                        req.setAttribute("returnUrl", req.getContextPath() + "/profile");
                        req.setAttribute("returnLabel", "Back to Profile");

                        req.getRequestDispatcher("/WEB-INF/views/ordersummary.jsp").forward(req, resp);
                        return;
                    }
                }
            } catch (NumberFormatException e) {
            }
        }
        resp.sendRedirect(req.getContextPath() + "/profile");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (!isUserLoggedIn(session)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User currentUser = (User) session.getAttribute("user");

        String userIdParam = req.getParameter("userId");
        int userId = Integer.parseInt(userIdParam);

        if (userId != currentUser.getUserId()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You can only edit your own profile.");
            return;
        }

        String firstName = req.getParameter("firstName");
        String lastName = req.getParameter("lastName");
        String email = req.getParameter("email");

        String addressIdStr = req.getParameter("addressId");
        Integer addressId = (addressIdStr != null && !addressIdStr.isEmpty()) ? Integer.parseInt(addressIdStr) : null;
        String street = req.getParameter("street");
        String streetNumber = req.getParameter("streetNumber");
        String postalCode = req.getParameter("postalCode");
        String city = req.getParameter("city");
        String province = req.getParameter("province");
        String country = req.getParameter("country");

        String paymentIdStr = req.getParameter("paymentId");
        Integer paymentId = (paymentIdStr != null && !paymentIdStr.isEmpty()) ? Integer.parseInt(paymentIdStr) : null;
        String cardNumber = req.getParameter("cardNumber");
        String expiryDate = req.getParameter("expiryDate");
        String cvc = req.getParameter("cvc");

        User tempUser = new User();
        tempUser.setUserId(userId);
        tempUser.setFirstName(firstName);
        tempUser.setLastName(lastName);
        tempUser.setEmail(email);

        Address tempAddress = new Address();
        tempAddress.setAddressId(addressId);
        tempAddress.setUser(currentUser);
        tempAddress.setStreet(street);
        tempAddress.setStreetNumber(streetNumber);
        tempAddress.setPostalCode(postalCode);
        tempAddress.setCity(city);
        tempAddress.setProvince(province);
        tempAddress.setCountry(country);

        PaymentInfo tempPayment = new PaymentInfo();
        tempPayment.setPaymentInfoId(paymentId);
        tempPayment.setCardNumber(cardNumber);
        tempPayment.setExpiryDate(expiryDate);
        tempPayment.setCvc(cvc);

        String errorMessage = validateProfileFields(firstName, lastName, email, street, streetNumber, postalCode, city, province, country, cardNumber, expiryDate, cvc);

        if (errorMessage != null) {
            forwardWithError(req, resp, errorMessage, tempUser, tempAddress, tempPayment);
            return;
        }

        try {
            if (addressId != null && addressId > 0) {
                addressService.updateAddress(tempAddress);
            } else {
                addressService.saveAddress(tempAddress);
            }

            User updatedUser = adminService.updateCustomerProfile(userId, firstName, lastName, email, addressId, street, streetNumber, postalCode, city, province, country, paymentId, cardNumber, expiryDate, cvc);

            session.setAttribute("user", updatedUser);

            resp.sendRedirect(req.getContextPath() + "/profile?updated=true");

        } catch (Exception e) {
            e.printStackTrace();
            forwardWithError(req, resp, "Update failed: " + e.getMessage(), tempUser, tempAddress, tempPayment);
        }
    }

    private void forwardWithError(HttpServletRequest req, HttpServletResponse resp, String error, User user, Address addr, PaymentInfo pay) throws ServletException, IOException {
        req.setAttribute("error", error);

        req.setAttribute("customer", user);
        req.setAttribute("customerAddress", addr);
        req.setAttribute("customerPayment", pay);

        req.setAttribute("formAction", "profile/edit");
        req.setAttribute("backUrl", "profile");

        req.getRequestDispatcher("/WEB-INF/views/profileform.jsp").forward(req, resp);
    }

    private String validateProfileFields(String firstName, String lastName, String email, String street, String streetNumber, String postalCode, String city, String province, String country, String cardNumber, String expiryDate, String cvc) {

        if (ValidationUtil.isEmpty(firstName) || ValidationUtil.isEmpty(lastName)) {
            return "First name and Last name are required.";
        }
        if (!ValidationUtil.isValidEmail(email)) {
            return "Invalid email address.";
        }

        if (ValidationUtil.isEmpty(street) || ValidationUtil.isEmpty(streetNumber) || ValidationUtil.isEmpty(postalCode) || ValidationUtil.isEmpty(city) || ValidationUtil.isEmpty(province) || ValidationUtil.isEmpty(country)) {
            return "Please fill in all address fields.";
        }

        if (!ValidationUtil.isTextOnly(street)) return "Street name cannot contain numbers.";
        if (!ValidationUtil.isNumbersOnly(streetNumber)) return "Street number must contain only digits.";
        if (!ValidationUtil.isValidPostalCode(postalCode)) return "Invalid Postal Code format.";
        if (!ValidationUtil.isTextOnly(province)) return "Province cannot contain numbers.";
        if (!ValidationUtil.isTextOnly(country)) return "Country cannot contain numbers.";

        if (ValidationUtil.isEmpty(cardNumber) || ValidationUtil.isEmpty(expiryDate) || ValidationUtil.isEmpty(cvc)) {
            return "Please fill in all payment details.";
        }

        if (!ValidationUtil.isValidCreditCard(cardNumber)) return "Card number must be exactly 16 digits.";
        if (!ValidationUtil.isExpiryDateInFuture(expiryDate))
            return "Invalid Expiry Date (Format MM/YY) or card expired.";
        if (!ValidationUtil.isValidCVC(cvc)) return "CVC must be exactly 3 digits.";

        return null;
    }
}