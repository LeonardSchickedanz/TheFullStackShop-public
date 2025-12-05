package yorku.thefullstackshop.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import yorku.thefullstackshop.models.ShoppingCart;
import yorku.thefullstackshop.models.User;
import yorku.thefullstackshop.services.implementations.ShoppingCartServiceImpl;
import yorku.thefullstackshop.services.interfaces.ShoppingCartService;

import java.io.IOException;

@WebServlet(name = "ShoppingCartController", urlPatterns = {"/shoppingcart", "/shoppingcart/update", "/shoppingcart/remove", "/shoppingcart/add"})
public class ShoppingCartController extends HttpServlet {

    private final ShoppingCartService shoppingCartService = new ShoppingCartServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(true);
        User user = (User) session.getAttribute("user");
        Integer userId = (user != null) ? user.getUserId() : null;
        String sessionId = session.getId();

        ShoppingCart cart = shoppingCartService.getCart(userId, sessionId);
        req.setAttribute("cart", cart);
        req.getRequestDispatcher("/WEB-INF/views/shoppingcart.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getServletPath();
        HttpSession session = req.getSession(true);
        User user = (User) session.getAttribute("user");
        Integer userId = (user != null) ? user.getUserId() : null;
        String sessionId = session.getId();

        String productIdStr = req.getParameter("productId");

        String returnUrl = req.getParameter("returnUrl");

        if (productIdStr != null) {
            try {
                int productId = Integer.parseInt(productIdStr);

                if ("/shoppingcart/add".equals(action)) {
                    int quantity = 1;
                    String quantityStr = req.getParameter("quantity");
                    if (quantityStr != null && !quantityStr.isEmpty()) {
                        quantity = Integer.parseInt(quantityStr);
                    }

                    try {
                        shoppingCartService.addItemToCart(userId, sessionId, productId, quantity);
                    } catch (RuntimeException e) {
                        if (e.getMessage() != null && e.getMessage().contains("Not enough inventory")) {
                            session.setAttribute("inventoryError", "We are sorry, but there is not enough stock for the requested quantity of product ID " + productId + ".");
                        } else {
                            throw e;
                        }
                    }

                    if (returnUrl != null && !returnUrl.trim().isEmpty()) {
                        resp.sendRedirect(returnUrl);
                    } else {
                        resp.sendRedirect(req.getContextPath() + "/shoppingcart");
                    }
                    return;

                } else if ("/shoppingcart/remove".equals(action)) {
                    shoppingCartService.removeFromCart(userId, sessionId, productId);
                } else if ("/shoppingcart/update".equals(action)) {
                    String quantityStr = req.getParameter("quantity");
                    if (quantityStr != null) {
                        int quantity = Integer.parseInt(quantityStr);

                        if (quantity > 0) {
                            try {
                                shoppingCartService.updateItemQuantity(userId, sessionId, productId, quantity);
                            } catch (RuntimeException e) {
                                if (e.getMessage() != null && e.getMessage().contains("Not enough inventory")) {
                                    session.setAttribute("inventoryError", "We are sorry, but the requested quantity for product ID " + productId + " exceeds available stock. The quantity was reset.");
                                } else {
                                    throw e;
                                }
                            }
                        } else {
                            shoppingCartService.removeFromCart(userId, sessionId, productId);
                        }
                    }
                }
            } catch (Exception e) {
                String errorMessage = "An unexpected error occurred during cart update.";
                if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                    errorMessage = e.getMessage();
                }
                session.setAttribute("inventoryError", errorMessage);
                e.printStackTrace();
            }
        }

        resp.sendRedirect(req.getContextPath() + "/shoppingcart");
    }
}