package yorku.thefullstackshop.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import yorku.thefullstackshop.models.Order;
import yorku.thefullstackshop.models.Role;
import yorku.thefullstackshop.models.User;
import yorku.thefullstackshop.services.implementations.OrderServiceImpl;
import yorku.thefullstackshop.services.interfaces.OrderService;

import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "OrderController", value = "/order")
public class OrderController extends HttpServlet {

    private final OrderService orderService;

    public OrderController() {
        this.orderService = new OrderServiceImpl();
    }

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        boolean isAdmin = user.getRole() != null && user.getRole().getRoleId() == Role.ADMIN_ID;

        String orderIdStr = request.getParameter("id");
        String from = request.getParameter("from");

        if (orderIdStr != null) {
            try {
                int orderId = Integer.parseInt(orderIdStr);
                Optional<Order> orderOpt = orderService.getOrderById(orderId);

                if (orderOpt.isPresent()) {
                    Order order = orderOpt.get();
                    boolean isOwner = order.getUser() != null && order.getUser().getUserId().equals(user.getUserId());

                    if (isOwner || isAdmin) {

                        if (order.getUser() != null) {
                            request.setAttribute("orderUser", order.getUser());
                        }

                        request.setAttribute("order", order);
                        request.setAttribute("isConfirmation", false);

                        if ("profile".equals(from)) {
                            request.setAttribute("returnUrl", request.getContextPath() + "/profile");
                            request.setAttribute("returnLabel", "Back to Profile");
                        } else {
                            request.setAttribute("returnUrl", request.getContextPath() + "/profile");
                            request.setAttribute("returnLabel", "Back to Profile");
                        }

                        request.getRequestDispatcher("/WEB-INF/views/ordersummary.jsp").forward(request, response);
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        if (isAdmin) {
            response.sendRedirect(request.getContextPath() + "/admin/sales");
        } else {
            response.sendRedirect(request.getContextPath() + "/profile");
        }
    }
}