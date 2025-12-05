package yorku.thefullstackshop.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import yorku.thefullstackshop.models.*;
import yorku.thefullstackshop.services.implementations.AddressServiceImpl;
import yorku.thefullstackshop.services.implementations.AdminServiceImpl;
import yorku.thefullstackshop.services.implementations.OrderServiceImpl;
import yorku.thefullstackshop.services.interfaces.AddressService;
import yorku.thefullstackshop.services.interfaces.OrderService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "AdminController", urlPatterns = {"/admin", "/admin/inventory", "/admin/sales", "/admin/customers", "/admin/customer/edit", "/admin/order"})
public class AdminController extends HttpServlet {

    private final AdminServiceImpl adminService;
    private final OrderService orderService;
    private final AddressService addressService;

    public AdminController() {
        this.adminService = new AdminServiceImpl();
        this.orderService = new OrderServiceImpl();
        this.addressService = new AddressServiceImpl();
    }

    private boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        User user = (User) session.getAttribute("user");
        return user != null && user.getRole() != null && user.getRole().getRoleId() == Role.ADMIN_ID;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String path = req.getServletPath();

        switch (path) {
            case "/admin/inventory":
                List<Product> products = adminService.getAllProducts();
                req.setAttribute("products", products);
                req.getRequestDispatcher("/WEB-INF/views/admin/inventory.jsp").forward(req, resp);
                break;

            case "/admin/sales":
                String email = req.getParameter("email");
                String prod = req.getParameter("product");
                String dateFrom = req.getParameter("dateFrom");
                String dateTo = req.getParameter("dateTo");

                List<Order> orders = orderService.getSalesHistory(email, prod, dateFrom, dateTo);
                req.setAttribute("orders", orders);
                req.getRequestDispatcher("/WEB-INF/views/admin/sales.jsp").forward(req, resp);
                break;

            case "/admin/order":
                String orderIdStr = req.getParameter("id");
                if (orderIdStr != null) {
                    try {
                        int orderId = Integer.parseInt(orderIdStr);
                        Optional<Order> orderOpt = orderService.getOrderById(orderId);

                        if (orderOpt.isPresent()) {
                            Order order = orderOpt.get();

                            if (order.getUser() != null) {
                                Optional<User> userOpt = adminService.getCustomerById(order.getUser().getUserId());
                                userOpt.ifPresent(u -> req.setAttribute("orderUser", u));
                            }

                            req.setAttribute("order", order);
                            req.setAttribute("isConfirmation", false);
                            req.setAttribute("returnUrl", req.getContextPath() + "/admin/sales");
                            req.setAttribute("returnLabel", "Back to Sales History");
                            req.getRequestDispatcher("/WEB-INF/views/ordersummary.jsp").forward(req, resp);
                        } else {
                            resp.sendRedirect(req.getContextPath() + "/admin/sales");
                        }
                    } catch (NumberFormatException e) {
                        resp.sendRedirect(req.getContextPath() + "/admin/sales");
                    }
                } else {
                    resp.sendRedirect(req.getContextPath() + "/admin/sales");
                }
                break;

            case "/admin/customers":
                String searchEmail = req.getParameter("searchEmail");
                List<User> users = adminService.getAllCustomers(searchEmail);
                req.setAttribute("users", users);
                req.getRequestDispatcher("/WEB-INF/views/admin/customers.jsp").forward(req, resp);
                break;

            case "/admin/customer/edit":
                String idStr = req.getParameter("id");
                if (idStr != null) {
                    try {
                        int userId = Integer.parseInt(idStr);

                        Optional<User> userOpt = adminService.getCustomerById(userId);
                        userOpt.ifPresent(u -> req.setAttribute("customer", u));

                        Optional<Address> addressOpt = adminService.getCustomerAddress(userId);
                        addressOpt.ifPresent(a -> req.setAttribute("customerAddress", a));

                        Optional<PaymentInfo> paymentOpt = adminService.getCustomerPayment(userId);
                        paymentOpt.ifPresent(p -> req.setAttribute("customerPayment", p));

                        List<Order> customerOrders = orderService.getOrdersByUserId(userId);
                        req.setAttribute("customerOrders", customerOrders);

                        req.setAttribute("formAction", "admin/customer/edit");
                        req.setAttribute("backUrl", "admin/customers");

                        req.getRequestDispatcher("/WEB-INF/views/admin/customerprofile.jsp").forward(req, resp);
                    } catch (NumberFormatException e) {
                        resp.sendRedirect(req.getContextPath() + "/admin/customers");
                    }
                } else {
                    resp.sendRedirect(req.getContextPath() + "/admin/customers");
                }
                break;

            default:
                resp.sendRedirect(req.getContextPath() + "/admin/sales");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String path = req.getServletPath();
        String userIdStr = req.getParameter("userId");
        int userId = 0;

        try {
            if (userIdStr != null) {
                userId = Integer.parseInt(userIdStr);
            }
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/admin/customers?error=InvalidID");
            return;
        }

        if ("/admin/inventory".equals(path)) {
            try {
                int productId = Integer.parseInt(req.getParameter("productId"));
                int quantity = Integer.parseInt(req.getParameter("quantity"));
                adminService.updateInventory(productId, quantity);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            resp.sendRedirect(req.getContextPath() + "/admin/inventory");

        } else if ("/admin/customer/edit".equals(path)) {

            try {
                String firstName = req.getParameter("firstName");
                String lastName = req.getParameter("lastName");
                String email = req.getParameter("email");

                String addressIdStr = req.getParameter("addressId");
                Integer addressId = (addressIdStr != null && !addressIdStr.trim().isEmpty()) ? Integer.parseInt(addressIdStr) : null;

                Optional<User> userOpt = adminService.getCustomerById(userId);

                if (userOpt.isPresent()) {
                    Address address = new Address();
                    address.setAddressId(addressId);
                    address.setUser(userOpt.get());
                    address.setStreet(req.getParameter("street"));
                    address.setStreetNumber(req.getParameter("streetNumber"));
                    address.setPostalCode(req.getParameter("postalCode"));
                    address.setCity(req.getParameter("city"));
                    address.setProvince(req.getParameter("province"));
                    address.setCountry(req.getParameter("country"));

                    if (addressId != null && addressId > 0) {
                        addressService.updateAddress(address);
                    } else {
                        addressService.saveAddress(address);
                    }
                }

                String paymentIdStr = req.getParameter("paymentId");
                Integer paymentId = (paymentIdStr != null && !paymentIdStr.trim().isEmpty()) ? Integer.parseInt(paymentIdStr) : null;
                String cardNumber = req.getParameter("cardNumber");
                String expiryDate = req.getParameter("expiryDate");
                String cvc = req.getParameter("cvc");

                User updatedUser = adminService.updateCustomerProfile(userId, firstName, lastName, email, addressId, req.getParameter("street"), req.getParameter("streetNumber"), req.getParameter("postalCode"), req.getParameter("city"), req.getParameter("province"), req.getParameter("country"), paymentId, cardNumber, expiryDate, cvc);

                HttpSession adminSession = req.getSession(false);
                User adminUser = (User) adminSession.getAttribute("user");
                if (updatedUser != null && adminUser != null && adminUser.getUserId().equals(updatedUser.getUserId())) {
                    adminSession.setAttribute("user", updatedUser);
                }

                resp.sendRedirect(req.getContextPath() + "/admin/customers?msg=Saved");

            } catch (Exception e) {
                e.printStackTrace();
                resp.sendRedirect(req.getContextPath() + "/admin/customer/edit?id=" + userId + "&error=UpdateFailed");
            }
        }
    }
}