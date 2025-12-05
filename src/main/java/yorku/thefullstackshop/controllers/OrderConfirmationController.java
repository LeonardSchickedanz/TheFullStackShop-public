package yorku.thefullstackshop.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import yorku.thefullstackshop.models.Order;
import yorku.thefullstackshop.models.User;
import yorku.thefullstackshop.services.implementations.OrderServiceImpl;
import yorku.thefullstackshop.services.interfaces.OrderService;

import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "OrderConfirmationController", value = "/order-confirmation")
public class OrderConfirmationController extends HttpServlet {

    private final OrderService orderService = new OrderServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");

        String orderIdStr = request.getParameter("id");

        if (orderIdStr != null) {
            try {
                int orderId = Integer.parseInt(orderIdStr);

                Optional<Order> orderOpt = orderService.getOrderById(orderId);

                if (orderOpt.isPresent()) {
                    Order order = orderOpt.get();

                    if (order.getUser().getUserId().equals(user.getUserId())) {
                        request.setAttribute("order", order);
                    } else {
                        response.sendRedirect(request.getContextPath() + "/profile");
                        return;
                    }
                }
            } catch (NumberFormatException e) {
            }
        }

        request.setAttribute("isConfirmation", true);

        request.getRequestDispatcher("/WEB-INF/views/ordersummary.jsp").forward(request, response);
    }
}