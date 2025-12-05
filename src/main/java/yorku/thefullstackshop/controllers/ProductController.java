package yorku.thefullstackshop.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import yorku.thefullstackshop.models.Product;
import yorku.thefullstackshop.models.User;
import yorku.thefullstackshop.services.implementations.CatalogServiceImpl;
import yorku.thefullstackshop.services.implementations.ShoppingCartServiceImpl;
import yorku.thefullstackshop.services.interfaces.CatalogService;
import yorku.thefullstackshop.services.interfaces.ShoppingCartService;

import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "ProductController", value = "/product")
public class ProductController extends HttpServlet {

    private final CatalogService catalogService = new CatalogServiceImpl();
    private final ShoppingCartService cartService = new ShoppingCartServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idParam = request.getParameter("id");

        if (idParam != null) {
            try {
                int productId = Integer.parseInt(idParam);
                Optional<Product> productOpt = catalogService.getProductById(productId);

                if (productOpt.isPresent()) {
                    request.setAttribute("product", productOpt.get());

                    HttpSession session = request.getSession(true);
                    User user = (User) session.getAttribute("user");
                    Integer userId = (user != null) ? user.getUserId() : null;
                    String sessionId = session.getId();

                    int count = cartService.getCartItemCount(userId, sessionId);
                    request.setAttribute("cartCount", count);

                    request.getRequestDispatcher("/WEB-INF/views/productdetails.jsp").forward(request, response);
                    return;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        response.sendRedirect(request.getContextPath() + "/catalog");
    }
}