package yorku.thefullstackshop.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import yorku.thefullstackshop.models.*;
import yorku.thefullstackshop.services.implementations.*;
import yorku.thefullstackshop.services.interfaces.*;
import yorku.thefullstackshop.utils.ValidationUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "CheckoutController", urlPatterns = {"/checkout", "/checkout/process", "/checkout/register"})
public class CheckoutController extends HttpServlet {

    private final UserService userService = new UserServiceImpl();
    private final ShoppingCartService cartService = new ShoppingCartServiceImpl();
    private final PaymentService paymentService = new PaymentServiceImpl();
    private final AddressService addressService = new AddressServiceImpl();

    private final CheckoutService checkoutService = new CheckoutServiceImpl();


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");

        Integer userId = null;
        String sessionId = session.getId();

        if (user != null) {
            userId = user.getUserId();
        }

        ShoppingCart cart = cartService.getCart(userId, sessionId);

        List<String> inventoryIssues = cartService.validateCartInventory(cart, sessionId);

        if (!inventoryIssues.isEmpty()) {
            String message = String.join("<br>", inventoryIssues);
            session.setAttribute("inventoryWarning", message);

            response.sendRedirect(request.getContextPath() + "/shoppingcart");
            return;
        }

        if (user != null) {
            Optional<Address> addressOpt = addressService.findLatestAddressByUserId(userId);
            addressOpt.ifPresent(address -> request.setAttribute("latestAddress", address));

            Optional<PaymentInfo> paymentOpt = paymentService.findLatestPaymentInfoByUserId(userId);
            paymentOpt.ifPresent(payment -> request.setAttribute("latestPayment", payment));
        }


        BigDecimal grandTotal = BigDecimal.ZERO;
        if (cart.getCartItems() != null) {
            for (CartItem item : cart.getCartItems()) {
                BigDecimal lineTotal = item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity()));
                grandTotal = grandTotal.add(lineTotal);
            }
        }

        request.setAttribute("cart", cart);
        request.setAttribute("grandTotal", grandTotal);

        request.getRequestDispatcher("/WEB-INF/views/checkout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getServletPath();

        if ("/checkout/register".equals(action)) {
            handleRegister(req, resp);
        } else if ("/checkout/process".equals(action)) {
            handleProcessOrder(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/checkout");
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String firstName = req.getParameter("firstName");
        String lastName = req.getParameter("lastName");
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (ValidationUtil.isEmpty(firstName) || ValidationUtil.isEmpty(lastName)) {
            redirectWithError(req, resp, "First name and Last name are required.");
            return;
        }

        if (!ValidationUtil.isValidEmail(email)) {
            redirectWithError(req, resp, "Invalid email address.");
            return;
        }

        if (!ValidationUtil.isValidPassword(password)) {
            redirectWithError(req, resp, "Password must be at least 6 characters and include a special character.");
            return;
        }

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(password);

        try {
            User savedUser = userService.registerUser(user);
            HttpSession session = req.getSession();
            session.setAttribute("user", savedUser);
            resp.sendRedirect(req.getContextPath() + "/checkout");
        } catch (Exception e) {
            redirectWithError(req, resp, "Registration failed: " + e.getMessage());
        }
    }

    private void handleProcessOrder(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User user = (User) session.getAttribute("user");

        String street = req.getParameter("street");
        String streetNumber = req.getParameter("streetNumber");
        String postalCode = req.getParameter("postalCode");
        String city = req.getParameter("city");
        String province = req.getParameter("province");
        String country = req.getParameter("country");

        String cardNumber = req.getParameter("cardNumber");
        String expiryDate = req.getParameter("expiryDate");
        String cvc = req.getParameter("cvc");

        Address tempAddress = new Address();
        tempAddress.setStreet(street);
        tempAddress.setStreetNumber(streetNumber);
        tempAddress.setPostalCode(postalCode);
        tempAddress.setCity(city);
        tempAddress.setProvince(province);
        tempAddress.setCountry(country);

        PaymentInfo tempPayment = new PaymentInfo();
        tempPayment.setCardNumber(cardNumber);
        tempPayment.setExpiryDate(expiryDate);
        tempPayment.setCvc(cvc);

        String errorMessage = validateCheckoutFields(street, streetNumber, postalCode, city, province, country, cardNumber, expiryDate, cvc);

        if (errorMessage != null) {
            forwardWithError(req, resp, errorMessage, tempAddress, tempPayment, user);
            return;
        }

        try {
            String wasEdited = req.getParameter("paymentWasEdited");
            boolean savePayment = !"true".equals(wasEdited);

            String addressWasEdited = req.getParameter("addressWasEdited");
            boolean saveAddress = !"true".equals(addressWasEdited);

            CheckoutServiceImpl checkoutServiceImpl = (CheckoutServiceImpl) checkoutService;
            Order order = checkoutServiceImpl.processCheckout(user, tempAddress, tempPayment, cvc, saveAddress, savePayment);

            resp.sendRedirect(req.getContextPath() + "/order-confirmation?id=" + order.getOrderId());

        } catch (Exception e) {
            e.printStackTrace();
            forwardWithError(req, resp, "Order failed: " + e.getMessage(), tempAddress, tempPayment, user);
        }
    }

    private void redirectWithError(HttpServletRequest req, HttpServletResponse resp, String message) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/checkout?error=" + java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8));
    }

    private void forwardWithError(HttpServletRequest req, HttpServletResponse resp, String error, Address addr, PaymentInfo pay, User user) throws ServletException, IOException {
        req.setAttribute("error", error);
        req.setAttribute("latestAddress", addr);
        req.setAttribute("latestPayment", pay);

        ShoppingCart cart = cartService.getCart(user.getUserId(), null);
        BigDecimal grandTotal = BigDecimal.ZERO;
        if (cart.getCartItems() != null) {
            for (CartItem item : cart.getCartItems()) {
                BigDecimal lineTotal = item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity()));
                grandTotal = grandTotal.add(lineTotal);
            }
        }
        req.setAttribute("cart", cart);
        req.setAttribute("grandTotal", grandTotal);

        req.getRequestDispatcher("/WEB-INF/views/checkout.jsp").forward(req, resp);
    }

    private String validateCheckoutFields(String street, String streetNumber, String postalCode, String city, String province, String country, String cardNumber, String expiryDate, String cvc) {

        if (ValidationUtil.isEmpty(street) || ValidationUtil.isEmpty(streetNumber) || ValidationUtil.isEmpty(postalCode) || ValidationUtil.isEmpty(city) || ValidationUtil.isEmpty(province) || ValidationUtil.isEmpty(country)) {
            return "Please fill in all shipping address fields.";
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