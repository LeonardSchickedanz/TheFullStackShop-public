package yorku.thefullstackshop.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import yorku.thefullstackshop.models.Brand;
import yorku.thefullstackshop.models.Category;
import yorku.thefullstackshop.models.Product;
import yorku.thefullstackshop.models.User;
import yorku.thefullstackshop.services.implementations.CatalogServiceImpl;
import yorku.thefullstackshop.services.implementations.ShoppingCartServiceImpl;
import yorku.thefullstackshop.services.interfaces.CatalogService;
import yorku.thefullstackshop.services.interfaces.ShoppingCartService;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "CatalogController", urlPatterns = {"/catalog", ""})
public class CatalogController extends HttpServlet {

    private final CatalogService catalogService = new CatalogServiceImpl();
    private final ShoppingCartService cartService = new ShoppingCartServiceImpl();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String search = request.getParameter("search");
        String sort = request.getParameter("sort");
        String brandIdStr = request.getParameter("brand");
        String categoryIdStr = request.getParameter("category");

        Integer brandId = null;
        Integer categoryId = null;

        if (brandIdStr != null && !brandIdStr.isEmpty()) {
            try {
                brandId = Integer.parseInt(brandIdStr);
            } catch (NumberFormatException e) {
            }
        }
        if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
            try {
                categoryId = Integer.parseInt(categoryIdStr);
            } catch (NumberFormatException e) {
            }
        }

        List<Product> products = catalogService.getProducts(search, brandId, categoryId, sort);
        List<Brand> brands = catalogService.getBrands();
        List<Category> categories = catalogService.getCategories();

        request.setAttribute("products", products);
        request.setAttribute("brands", brands);
        request.setAttribute("categories", categories);

        request.setAttribute("selectedSearch", search);
        request.setAttribute("selectedBrand", brandId);
        request.setAttribute("selectedCategory", categoryId);
        request.setAttribute("selectedSort", sort);

        HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        Integer userId = (user != null) ? user.getUserId() : null;
        String sessionId = session.getId();

        int count = cartService.getCartItemCount(userId, sessionId);
        request.setAttribute("cartCount", count);

        request.getRequestDispatcher("/WEB-INF/views/catalog.jsp").forward(request, response);
    }
}